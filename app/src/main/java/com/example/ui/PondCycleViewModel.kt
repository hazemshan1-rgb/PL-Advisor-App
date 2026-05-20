package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.PondCycle
import com.example.data.PondCycleDatabase
import com.example.data.PondCycleRepository
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
        // Automatically fetch or initialize a default cycle if none exist
        viewModelScope.launch {
            allCycles.filter { it.isNotEmpty() }.firstOrNull()?.let { list ->
                if (_activeCycle.value == null) {
                    _activeCycle.value = list.first()
                }
            } ?: run {
                // If list is empty, pre-populate default cycle
                val default = PondCycle(pondName = "Seaside Pond Alpha")
                val idLong = repository.insert(default)
                _activeCycle.value = default.copy(id = idLong.toInt())
            }
        }
    }

    fun selectCycle(cycle: PondCycle) {
        _activeCycle.value = cycle
    }

    fun createNewPond(name: String, size: Double, density: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCycle = PondCycle(
                pondName = name,
                pondSize = size,
                proposedDensity = density,
                stockingDate = System.currentTimeMillis()
            )
            val newId = repository.insert(newCycle)
            _activeCycle.value = newCycle.copy(id = newId.toInt())
        }
    }

    fun deleteCycle(cycle: PondCycle) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(cycle)
            // If the deleted cycle was the active one, pick another
            if (_activeCycle.value?.id == cycle.id) {
                val remnants = allCycles.value.filter { it.id != cycle.id }
                if (remnants.isNotEmpty()) {
                    _activeCycle.value = remnants.first()
                } else {
                    // Create an empty fallback
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
                val updated = updater(it)
                // Debounce/auto save to database
                viewModelScope.launch(Dispatchers.IO) {
                    repository.update(updated)
                }
                updated
            }
        }
    }

    // Calculations streams
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
                laborCost = it.laborCostPerDay
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
