package com.shrimpadvisor.plcycle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shrimpadvisor.plcycle.BuildConfig
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.data.PondCycleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PondCycleViewModel(
    application: Application,
    private val repository: PondCycleRepository
) : AndroidViewModel(application) {

    val allCycles: StateFlow<List<PondCycle>> = repository.allCycles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeCycle = MutableStateFlow<PondCycle?>(null)
    val activeCycle: StateFlow<PondCycle?> = _activeCycle.asStateFlow()

    init {
        viewModelScope.launch {
            // Use repository.allCycles directly (cold Flow) so we wait for Room to emit
            // the actual DB state rather than the StateFlow's emptyList() initial value.
            val firstEmission = repository.allCycles.first()
            if (firstEmission.isNotEmpty()) {
                if (_activeCycle.value == null) {
                    _activeCycle.value = firstEmission.first()
                }
            } else {
                val default = PondCycle(pondName = "Seaside Pond Alpha")
                val idLong = repository.insert(default)
                _activeCycle.value = default.copy(id = idLong.toInt())
            }
        }
    }

    // Daily readings for the currently active cycle
    val activeReadings: StateFlow<List<DailyReading>> = activeCycle
        .flatMapLatest { cycle ->
            if (cycle != null) {
                repository.getReadingsForCycle(cycle.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun selectCycle(cycle: PondCycle) {
        _activeCycle.value = cycle
    }

    fun createNewPond(name: String, size: Double, density: Double) {
        val safeName = name.trim().take(80).ifBlank { "New Pond" }
        val safeSize = size.coerceIn(10.0, 100_000.0)
        val safeDensity = density.coerceIn(1.0, 500.0)
        viewModelScope.launch(Dispatchers.IO) {
            val newCycle = PondCycle(
                pondName = safeName,
                pondSize = safeSize,
                proposedDensity = safeDensity,
                stockingDate = System.currentTimeMillis()
            )
            val newId = repository.insert(newCycle)
            _activeCycle.value = newCycle.copy(id = newId.toInt())
        }
    }

    fun deleteCycle(cycle: PondCycle) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(cycle)
            if (_activeCycle.value?.id == cycle.id) {
                val remnants = allCycles.value.filter { it.id != cycle.id }
                if (remnants.isNotEmpty()) {
                    _activeCycle.value = remnants.first()
                } else {
                    val fallback = PondCycle(pondName = "Main Nursery Delta")
                    val idLoc = repository.insert(fallback)
                    _activeCycle.value = fallback.copy(id = idLoc.toInt())
                }
            }
        }
    }

    fun updateActiveCycle(updater: (PondCycle) -> PondCycle) {
        _activeCycle.update { current ->
            current?.let {
                val raw = updater(it)
                val sanitized = raw.copy(
                    pondName = raw.pondName.trim().take(80).ifBlank { it.pondName },
                    pondSize = raw.pondSize.coerceIn(10.0, 100_000.0),
                    proposedDensity = raw.proposedDensity.coerceIn(1.0, 500.0),
                    ph = raw.ph.coerceIn(4.0, 11.0),
                    doLevel = raw.doLevel.coerceIn(0.0, 30.0),
                    salinity = raw.salinity.coerceIn(0.0, 45.0),
                    temp = raw.temp.coerceIn(5.0, 45.0),
                    tanLevel = raw.tanLevel.coerceIn(0.0, 20.0),
                    estimatedSurvival = raw.estimatedSurvival.coerceIn(0.0, 100.0),
                    currentAbw = raw.currentAbw.coerceAtLeast(0.0),
                    currentAge = raw.currentAge.coerceAtLeast(0),
                    totalFeedConsumed = raw.totalFeedConsumed.coerceAtLeast(0.0),
                    averageDailyGain = raw.averageDailyGain.coerceIn(0.0, 5.0),
                    feedCostPerKg = raw.feedCostPerKg.coerceAtLeast(0.0),
                    plUnitCost = raw.plUnitCost.coerceAtLeast(0.0)
                )
                viewModelScope.launch(Dispatchers.IO) {
                    repository.update(sanitized)
                }
                sanitized
            }
        }
    }

    fun logDailyReading() {
        val cycle = _activeCycle.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDailyReading(
                DailyReading(
                    pondCycleId = cycle.id,
                    pondAge = cycle.currentAge,
                    survivalPct = cycle.estimatedSurvival,
                    doLevel = cycle.doLevel,
                    tanLevel = cycle.tanLevel,
                    ph = cycle.ph,
                    temp = cycle.temp,
                    abw = cycle.currentAbw
                )
            )
        }
    }

    fun sendChatMessage(question: String) {
        val cycle = _activeCycle.value ?: return
        val userMsg = ChatMessage(text = question, isUser = true)
        _chatMessages.update { it + userMsg }
        _isAiLoading.value = true
        viewModelScope.launch {
            val last14Readings = activeReadings.value
                .sortedBy { it.pondAge }
                .takeLast(14)
            val reply = GeminiAdvisor.ask(BuildConfig.GEMINI_API_KEY, cycle, question, last14Readings)
            _chatMessages.update { it + ChatMessage(text = reply, isUser = false) }
            _isAiLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    // Calculation streams
    val plQualityResult = activeCycle.map { cycle ->
        cycle?.let {
            AdvisorEngine.evaluatePLQuality(
                stressTolerance = it.stressToleranceScore,
                gutFullness = it.gutFullnessScore,
                supplierScore = it.supplierScore
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val stockingResult = activeCycle.map { cycle ->
        cycle?.let {
            AdvisorEngine.evaluateStocking(
                pondSize = it.pondSize,
                proposedDensity = it.proposedDensity,
                targetWeight = it.harvestWeightTarget,
                oxygen = it.doLevel,
                ph = it.ph,
                salinity = it.salinity,
                temp = it.temp,
                tan = it.tanLevel
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val survivalResult = activeCycle.map { cycle ->
        cycle?.let {
            AdvisorEngine.evaluateSurvival(
                age = it.currentAge,
                estimatedSurvival = it.estimatedSurvival,
                oxygen = it.doLevel,
                tan = it.tanLevel,
                ph = it.ph
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val costResult = activeCycle.map { cycle ->
        cycle?.let {
            AdvisorEngine.evaluateCosts(
                pondSize = it.pondSize,
                proposedDensity = it.proposedDensity,
                estimatedSurvival = it.estimatedSurvival,
                currentAbw = it.currentAbw,
                age = it.currentAge,
                totalFeed = it.totalFeedConsumed,
                plUnitCost = it.plUnitCost,
                feedCostPerKg = it.feedCostPerKg,
                aerationCost = it.aerationCostPerDay,
                probioticCost = it.probioticCostPerDay,
                laborCost = it.laborCostPerDay
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val harvestResult = activeCycle.map { cycle ->
        cycle?.let {
            AdvisorEngine.optimizeHarvest(
                pondSize = it.pondSize,
                proposedDensity = it.proposedDensity,
                estimatedSurvival = it.estimatedSurvival,
                currentAbw = it.currentAbw,
                totalFeed = it.totalFeedConsumed,
                adg = it.averageDailyGain,
                feedCostPerKg = it.feedCostPerKg,
                aerationCost = it.aerationCostPerDay,
                probioticCost = it.probioticCostPerDay,
                laborCost = it.laborCostPerDay,
                mortalityRatePerDay = it.customMortalityRate,
                mortalityAcceleration = it.mortalityAcceleration
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

class PondCycleViewModelFactory(
    private val application: Application,
    private val repository: PondCycleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PondCycleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PondCycleViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
