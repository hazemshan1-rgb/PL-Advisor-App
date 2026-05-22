package com.shrimpadvisor.plcycle.ui

import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.data.RegionProfile
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object AdvisorEngine {
    
    // ----------------------------------------------------
    // Module 1: PL Pre-Stocking Quality Gate
    // ----------------------------------------------------
    enum class QualityVerdict {
        STOCK, HOLD, REJECT
    }
    
    data class QualityResult(
        val score: Double,
        val verdict: QualityVerdict,
        val statusMessage: String,
        val week1SurvivalBaseline: Double,
        val recommendations: List<String>
    )

    fun evaluatePLQuality(
        stressTolerance: Int,
        gutFullness: Int,
        supplierScore: Int
    ): QualityResult {
        val score = (stressTolerance + gutFullness + supplierScore) / 3.0
        val verdict = when {
            score >= 80.0 -> QualityVerdict.STOCK
            score >= 60.0 -> QualityVerdict.HOLD
            else -> QualityVerdict.REJECT
        }
        
        val baseline = when (verdict) {
            QualityVerdict.STOCK -> 85.0
            QualityVerdict.HOLD -> 75.0
            QualityVerdict.REJECT -> 50.0
        }
        
        val recommendations = mutableListOf<String>()
        val hasLowStress = stressTolerance < 75
        val hasLowGut = gutFullness < 75
        val hasLowSupplier = supplierScore < 70
        
        if (verdict == QualityVerdict.STOCK) {
            recommendations.add("Consignment is premium. Ready for direct stocking.")
            if (hasLowStress) recommendations.add("Slight stress tolerance weakness: Acclimate PL components for an extra 30–60 minutes in water elements prior to release.")
            if (hasLowGut) recommendations.add("Slight feed reservation: Apply feed inside acclimation tank immediately.")
        } else if (verdict == QualityVerdict.HOLD) {
            recommendations.add("HOLD shipment in storage or quarantine nursery for 24–48 hours to acclimate.")
            if (hasLowStress) recommendations.add("Perform gradual salinity matching (ideal delta < 2 ppt per hour) to rebuild osmotic resilience.")
            if (hasLowGut) recommendations.add("Increase feeding frequency of Artemia or micro-diet in nursery prior to stocking.")
            if (hasLowSupplier) recommendations.add("Be cautious of pathogen levels. Ask for SPF certificates.")
        } else {
            recommendations.add("CRITICAL REJECT. Extreme risk of early mortality syndrome (EMS) or osmotic shock.")
            recommendations.add("Do NOT stock this batch. Re-verify health tags, or reject the consignment entirely.")
        }
        
        val msg = when (verdict) {
            QualityVerdict.STOCK -> "STOCK: Excellent consignment quality! Week-1 survival estimated at ${baseline.toInt()}%."
            QualityVerdict.HOLD -> "HOLD & ADJUST: Sub-optimal health variables. Holding or quarantine recommended."
            QualityVerdict.REJECT -> "ABORT / REJECT: Dangerous quality score. High probability of early crop collapse."
        }
        
        return QualityResult(score, verdict, msg, baseline, recommendations)
    }

    // ----------------------------------------------------
    // Module 2: Stocking Decision Engine
    // ----------------------------------------------------
    data class WaterParameterCheck(
        val name: String,
        val valueString: String,
        val isOptimal: Boolean,
        val rangeMessage: String
    )

    data class StockingResult(
        val maxSafeBiomass: Double, // kg
        val optimalDensity: Double, // PL/m²
        val totalOptimalQty: Double, // PLs total
        val proposedTotalQty: Double,
        val proposedBiomass: Double,
        val carryingCapacityExceeded: Boolean,
        val safetyMarginPercent: Double,
        val parameterChecks: List<WaterParameterCheck>
    )

    fun evaluateStocking(
        pondSize: Double, // m²
        proposedDensity: Double, // PL/m²
        targetWeight: Double, // g
        oxygen: Double,
        ph: Double,
        salinity: Double,
        temp: Double,
        tan: Double
    ): StockingResult {
        // Carrying capacity logic
        val carryingCapacityRatio = 6.0 // kg/m²
        val maxSafeBiomass = pondSize * carryingCapacityRatio * 0.80
        
        // Assume standard survival target over full cycle is 75%
        val optimalDensity = Math.floor(maxSafeBiomass / (pondSize * (targetWeight / 1000.0) * 0.75))
        val optimalDensityClamped = max(10.0, optimalDensity)
        
        val totalOptimalQty = optimalDensityClamped * pondSize
        val proposedTotalQty = proposedDensity * pondSize
        val proposedBiomass = proposedTotalQty * (targetWeight / 1000.0) * 0.75
        
        val carryingCapacityExceeded = proposedBiomass > maxSafeBiomass
        val safetyMarginPercent = ((maxSafeBiomass - proposedBiomass) / maxSafeBiomass * 100.0)
        
        // Parameter checks
        val checks = listOf(
            WaterParameterCheck(
                name = "Dissolved Oxygen (DO)",
                valueString = String.format("%.1f ppm", oxygen),
                isOptimal = oxygen >= 5.5,
                rangeMessage = "Optimal: ≥ 5.5 ppm (Hard Floor)"
            ),
            WaterParameterCheck(
                name = "pH",
                valueString = String.format("%.1f", ph),
                isOptimal = ph in 7.5..8.5,
                rangeMessage = "Optimal: 7.5 – 8.5"
            ),
            WaterParameterCheck(
                name = "Salinity",
                valueString = String.format("%.1f ppt", salinity),
                isOptimal = salinity in 15.0..35.0,
                rangeMessage = "Optimal: 15.0 – 35.0 ppt"
            ),
            WaterParameterCheck(
                name = "Temperature",
                valueString = String.format("%.1f °C", temp),
                isOptimal = temp in 26.0..32.0,
                rangeMessage = "Optimal: 26.0 – 32.0 °C"
            ),
            WaterParameterCheck(
                name = "Ammonia (TAN)",
                valueString = String.format("%.1f ppm", tan),
                isOptimal = tan < 1.0,
                rangeMessage = "Optimal: < 1.0 ppm"
            )
        )
        
        return StockingResult(
            maxSafeBiomass = maxSafeBiomass,
            optimalDensity = optimalDensityClamped,
            totalOptimalQty = totalOptimalQty,
            proposedTotalQty = proposedTotalQty,
            proposedBiomass = proposedBiomass,
            carryingCapacityExceeded = carryingCapacityExceeded,
            safetyMarginPercent = safetyMarginPercent,
            parameterChecks = checks
        )
    }

    // ----------------------------------------------------
    // Module 3: Survival Trajectory Monitor
    // ----------------------------------------------------
    enum class SurvivalStatus {
        GREEN, YELLOW, RED
    }
    
    data class SurvivalTrajectoryResult(
        val expectedSurvival: Double,
        val actualSurvival: Double,
        val deviation: Double,
        val status: SurvivalStatus,
        val classification: String,
        val diagnostics: List<String>,
        val actionSteps: List<String>
    )

    fun evaluateSurvival(
        age: Int,
        estimatedSurvival: Double,
        oxygen: Double,
        tan: Double,
        ph: Double
    ): SurvivalTrajectoryResult {
        // Linearly decays from 92% (at Day 7) to 75% at Day 30
        val ageClamped = max(1, min(30, age))
        val expectedSurvival = if (ageClamped <= 7) {
            92.0
        } else {
            92.0 - ((ageClamped - 7) / 23.0) * (92.0 - 75.0)
        }
        
        val deviation = estimatedSurvival - expectedSurvival
        val status = when {
            deviation >= -5.0 -> SurvivalStatus.GREEN
            deviation >= -15.0 -> SurvivalStatus.YELLOW
            else -> SurvivalStatus.RED
        }
        
        // Diagnostic decision logic for deviations
        var classification = "Normal"
        val diagnostics = mutableListOf<String>()
        val actionSteps = mutableListOf<String>()
        
        if (status == SurvivalStatus.GREEN) {
            classification = "Normal"
            diagnostics.add("Survival levels meet or exceed expected industry reference curves.")
            actionSteps.add("Maintain current feeding regimen and standard dynamic monitoring.")
            actionSteps.add("Monitor water indices weekly or in response to major weather occurrences.")
        } else {
            val isEnvFault = oxygen < 5.3 || tan >= 0.8 || ph < 7.3 || ph > 8.7
            if (isEnvFault) {
                classification = "Environmental"
                diagnostics.add("Water metrics are out-of-bounds, driving high physiological stress.")
                if (oxygen < 5.3) diagnostics.add("Oxygen levels ($oxygen ppm) are approaching critical hypoxia thresholds.")
                if (tan >= 0.8) diagnostics.add("Toxic ammonia ($tan ppm) is threatening gill respiratory efficiency.")
                
                actionSteps.add("Engage all oxygen injection devices and active aerator elements immediately.")
                actionSteps.add("Cut feeding rates by 50% for 48 hours to prevent compounding organic sediment loading.")
                actionSteps.add("Introduce clean, brackish water inflows (minimum 10% daily swap) to dilute ammonia concentrations.")
                actionSteps.add("Apply agricultural lime (10–12 kg per 1000m² element) if pH shows excessive shift patterns.")
            } else {
                classification = "Pathogenic"
                diagnostics.add("High mortality under stable physical parameters points to severe infection risks (Vibrio, Early Mortality Syndrome (EMS), or White Spot Syndrome).")
                
                actionSteps.add("Discontinue fresh live feed feed elements completely. Drop operational pellets by 40%.")
                actionSteps.add("Apply massive load of high-concentration bacillus spores (probiotics) to the pond water column.")
                actionSteps.add("Sample 50-100 random post-larvae and perform PCR diagnosis for WSSV / AHPND lab test.")
                actionSteps.add("Establish a quarantine perimeter. Disinfect net mesh and boots with chlorine to limit cross-pond transmission.")
            }
        }
        
        return SurvivalTrajectoryResult(
            expectedSurvival = expectedSurvival,
            actualSurvival = estimatedSurvival,
            deviation = deviation,
            status = status,
            classification = classification,
            diagnostics = diagnostics,
            actionSteps = actionSteps
        )
    }

    // ----------------------------------------------------
    // Module 4: Real-Time Cost/kg Tracker
    // ----------------------------------------------------
    data class CostTrackingResult(
        val totalFeedCost: Double,
        val totalPlCost: Double,
        val totalOperationalCost: Double,
        val totalAccumulatedCost: Double,
        val currentBiomass: Double, // kg
        val fcr: Double,
        val costPerKg: Double,
        val fcrStatusMessage: String,
        val fcrStatusColor: String
    )

    fun evaluateCosts(
        pondSize: Double,
        proposedDensity: Double,
        estimatedSurvival: Double,
        currentAbw: Double,
        age: Int,
        totalFeed: Double,
        plUnitCost: Double,
        feedCostPerKg: Double,
        aerationCost: Double,
        probioticCost: Double,
        laborCost: Double
    ): CostTrackingResult {
        val initialStockingQty = proposedDensity * pondSize
        val currentShrimpCount = initialStockingQty * (estimatedSurvival / 100.0)
        
        val currentBiomass = max(1.0, currentShrimpCount * (currentAbw / 1000.0))
        val fcr = totalFeed / currentBiomass
        
        val fcrStatus = when {
            fcr < 1.1 -> Pair("Extremely low FCR. Please audit stock counts or weight scales.", "warning")
            fcr in 1.1..1.5 -> Pair("Target FCR range of [1.1 - 1.5] met. Splendid feed convertibility.", "success")
            fcr in 1.51..1.8 -> Pair("Elevated FCR warning. Possible over-portioning or mortalities.", "warning")
            else -> Pair("Highly inefficient FCR. Severe economic loss imminent. Adjust feeding or check survival density.", "critical")
        }
        
        val totalPlCost = initialStockingQty * plUnitCost
        val totalFeedCost = totalFeed * feedCostPerKg
        val totalOperationalCost = age * (aerationCost + probioticCost + laborCost)
        val totalAccumulatedCost = totalPlCost + totalFeedCost + totalOperationalCost
        val costPerKg = totalAccumulatedCost / currentBiomass
        
        return CostTrackingResult(
            totalPlCost = totalPlCost,
            totalFeedCost = totalFeedCost,
            totalOperationalCost = totalOperationalCost,
            totalAccumulatedCost = totalAccumulatedCost,
            currentBiomass = currentBiomass,
            fcr = fcr,
            costPerKg = costPerKg,
            fcrStatusMessage = fcrStatus.first,
            fcrStatusColor = fcrStatus.second
        )
    }

    // ----------------------------------------------------
    // Module 5: Harvest Window Optimizer
    // ----------------------------------------------------
    data class HoldScenario(
        val day: Int,
        val projectedWeight: Double,
        val projectedBiomass: Double,
        val projectedPricePerKg: Double,
        val totalAddedCost: Double,
        val netAddedGain: Double
    )

    data class HarvestOptimizerResult(
        val currentWeight: Double,
        val currentBiomass: Double,
        val currentPricePerKg: Double,
        val currentRevenue: Double,
        val bestHoldScenario: HoldScenario?,
        val shouldHarvestNow: Boolean,
        val profitDifferential: Double,
        val holdScenariosList: List<HoldScenario>,
        val mortalityRatePerDay: Double = 0.004,
        val mortalityAcceleration: Double = 0.0,
        val scenarioResult: HarvestOptimizerResult? = null
    )

    /**
     * Returns price per kg for a given shrimp weight.
     *
     * When a [RegionProfile] is supplied, linearly interpolates between the five
     * size brackets (20 g, 25 g, 30 g, 35 g, 40 g).  Values below 20 g are
     * extrapolated using the 20–25 g slope; values above 40 g are extrapolated
     * using the 35–40 g slope.
     *
     * When no profile is provided the legacy formula is used as a fallback.
     */
    fun getPricePerKg(weightIng: Double, regionProfile: RegionProfile? = null): Double {
        val wt = max(2.0, weightIng)

        if (regionProfile == null) {
            // Legacy fallback
            return 4.0 + (wt * 0.25)
        }

        // Bracket definitions: (weight_g, price_per_kg)
        val brackets = listOf(
            20.0 to regionProfile.priceAt20g,
            25.0 to regionProfile.priceAt25g,
            30.0 to regionProfile.priceAt30g,
            35.0 to regionProfile.priceAt35g,
            40.0 to regionProfile.priceAt40g
        )

        // Find enclosing bracket pair and interpolate
        for (i in 0 until brackets.size - 1) {
            val (w0, p0) = brackets[i]
            val (w1, p1) = brackets[i + 1]
            if (wt <= w1) {
                val t = if (w1 == w0) 0.0 else (wt - w0) / (w1 - w0)
                return p0 + t * (p1 - p0)
            }
        }

        // Extrapolate above 40 g using the last two brackets
        val (w0, p0) = brackets[brackets.size - 2]
        val (w1, p1) = brackets[brackets.size - 1]
        val slope = (p1 - p0) / (w1 - w0)
        return p1 + slope * (wt - w1)
    }

    fun optimizeHarvest(
        pondSize: Double,
        proposedDensity: Double,
        estimatedSurvival: Double,
        currentAbw: Double,
        totalFeed: Double,
        adg: Double,
        feedCostPerKg: Double,
        aerationCost: Double,
        probioticCost: Double,
        laborCost: Double,
        currentAge: Int = 1,
        mortalityRatePerDay: Double = 0.004,
        mortalityAcceleration: Double = 0.0,
        regionProfile: RegionProfile? = null
    ): HarvestOptimizerResult {
        val initialStockingQty = proposedDensity * pondSize
        val currentShrimpQty = initialStockingQty * (estimatedSurvival / 100.0)

        val currentBiomass = max(1.0, currentShrimpQty * (currentAbw / 1000.0))
        val currentPrice = getPricePerKg(currentAbw, regionProfile)
        val currentRevenue = currentBiomass * currentPrice

        // Bug fix: derive daily feed rate from actual historical consumption instead of hardcoded 2.5%
        val historicalDailyFeedRate = if (currentAge > 0 && currentBiomass > 0) {
            (totalFeed / currentAge.toDouble() / currentBiomass).coerceIn(0.01, 0.08)
        } else 0.03

        fun runSimulation(baseRate: Double, acceleration: Double): HarvestOptimizerResult {
            val scenariosList = mutableListOf<HoldScenario>()
            var accumulatedCost = 0.0

            for (day in 1..30) {
                val survivorFraction = (1..day).fold(1.0) { acc, d ->
                    acc * (1.0 - (baseRate + acceleration * (d / 7.0)))
                }
                val projectedQty = currentShrimpQty * survivorFraction
                val projectedWeight = currentAbw + adg * day
                val projectedBiomass = projectedQty * (projectedWeight / 1000.0)
                val projectedPrice = getPricePerKg(projectedWeight, regionProfile)
                val projectedRevenue = projectedBiomass * projectedPrice

                val dailyFeedCost = projectedBiomass * historicalDailyFeedRate * feedCostPerKg
                accumulatedCost += aerationCost + probioticCost + laborCost + dailyFeedCost

                val netAddedGain = projectedRevenue - currentRevenue - accumulatedCost

                scenariosList.add(
                    HoldScenario(
                        day = day,
                        projectedWeight = projectedWeight,
                        projectedBiomass = projectedBiomass,
                        projectedPricePerKg = projectedPrice,
                        totalAddedCost = accumulatedCost,
                        netAddedGain = netAddedGain
                    )
                )
            }

            val bestScenario = scenariosList.maxByOrNull { it.netAddedGain }
            val shouldHarvestNow = (bestScenario == null || bestScenario.netAddedGain <= 0.0)
            val profitDifferential = if (shouldHarvestNow) 0.0 else (bestScenario?.netAddedGain ?: 0.0)

            return HarvestOptimizerResult(
                currentWeight = currentAbw,
                currentBiomass = currentBiomass,
                currentPricePerKg = currentPrice,
                currentRevenue = currentRevenue,
                bestHoldScenario = bestScenario,
                shouldHarvestNow = shouldHarvestNow,
                profitDifferential = profitDifferential,
                holdScenariosList = scenariosList,
                mortalityRatePerDay = baseRate,
                mortalityAcceleration = acceleration,
                scenarioResult = null
            )
        }

        val normalResult = runSimulation(mortalityRatePerDay, mortalityAcceleration)
        val diseaseScenarioRate = mortalityRatePerDay * 2.5
        val diseaseResult = runSimulation(diseaseScenarioRate, mortalityAcceleration)

        return normalResult.copy(scenarioResult = diseaseResult)
    }

    // ----------------------------------------------------
    // Module 6: Daily Feed Recommendation Engine (T1.1)
    // ----------------------------------------------------
    data class FeedRecommendation(
        val recommendedKgPerDay: Double,
        val feedRatePct: Double,
        val adjustmentNote: String
    )

    fun recommendDailyFeed(biomassKg: Double, tempC: Double): FeedRecommendation {
        val baseRate = 0.04
        val tempAdj = when {
            tempC < 22.0 -> -0.02
            tempC < 26.0 -> -0.01 * ((26.0 - tempC) / 4.0)
            tempC > 32.0 -> -0.005
            else -> 0.0
        }
        val adjustedRate = (baseRate + tempAdj).coerceIn(0.01, 0.06)
        val note = when {
            tempC < 22.0 -> "Reduced 50% — temp ${tempC}°C suppresses metabolism"
            tempC < 26.0 -> "Reduced — temp ${tempC}°C below optimal 26°C"
            tempC > 32.0 -> "Slightly reduced — temp ${tempC}°C above 32°C"
            else -> "Standard 4% rate — temp ${tempC}°C is optimal"
        }
        return FeedRecommendation(
            recommendedKgPerDay = biomassKg * adjustedRate,
            feedRatePct = adjustedRate * 100.0,
            adjustmentNote = note
        )
    }

    // ----------------------------------------------------
    // Module 7: Predictive Mortality Alert (T1.2)
    // ----------------------------------------------------
    data class SurvivalForecast(
        val forecastDay3: Double,
        val forecastDay7: Double,
        val slope: Double,
        val trend: String,
        val alertThreshold: Double,
        val isAlert: Boolean
    )

    fun predictSurvival(
        readings: List<DailyReading>,
        alertThreshold: Double = 85.0
    ): SurvivalForecast? {
        val recent = readings.sortedBy { it.pondAge }.takeLast(7)
        if (recent.size < 3) return null

        val n = recent.size.toDouble()
        val sumX = recent.sumOf { it.pondAge.toDouble() }
        val sumY = recent.sumOf { it.survivalPct }
        val sumXY = recent.sumOf { it.pondAge.toDouble() * it.survivalPct }
        val sumX2 = recent.sumOf { it.pondAge.toDouble().pow(2) }
        val denom = n * sumX2 - sumX.pow(2)

        if (denom == 0.0) return null

        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n
        val lastAge = recent.last().pondAge.toDouble()

        val day3 = (slope * (lastAge + 3) + intercept).coerceIn(0.0, 100.0)
        val day7 = (slope * (lastAge + 7) + intercept).coerceIn(0.0, 100.0)

        val trend = when {
            slope < -0.5 -> "Declining rapidly (${String.format("%.2f", slope)}%/day)"
            slope < -0.1 -> "Declining (${String.format("%.2f", slope)}%/day)"
            slope < 0.1  -> "Stable"
            else         -> "Improving (${String.format("%.2f", slope)}%/day)"
        }

        return SurvivalForecast(day3, day7, slope, trend, alertThreshold, day3 < alertThreshold)
    }

    // ----------------------------------------------------
    // Module 8: Temperature-Adjusted FCR (T1.5)
    // ----------------------------------------------------
    fun adjustFcrForTemp(rawFcr: Double, tempC: Double): Double {
        val factor = if (tempC < 26.0) 1.0 + 0.02 * (26.0 - tempC) else 1.0
        return rawFcr * factor
    }

    // ----------------------------------------------------
    // Module 9: Disease Risk Composite Score (T2.3)
    // ----------------------------------------------------
    data class DiseaseRisk(
        val score: Int,
        val level: String,
        val factors: List<String>
    )

    fun calculateDiseaseRisk(
        currentAge: Int,
        estimatedSurvival: Double,
        tanLevel: Double,
        temp: Double,
        doLevel: Double,
        ph: Double
    ): DiseaseRisk {
        val factors = mutableListOf<String>()
        var score = 0

        val ageScore = when {
            currentAge < 10 -> 5
            currentAge < 20 -> 10
            currentAge < 40 -> 20
            currentAge < 60 -> 15
            else -> 10
        }
        score += ageScore

        val tanScore = when {
            tanLevel >= 1.0 -> 30
            tanLevel >= 0.8 -> 20
            tanLevel >= 0.5 -> 10
            else -> 0
        }
        if (tanScore > 0) factors.add("High TAN: ${String.format("%.2f", tanLevel)} ppm")
        score += tanScore

        val expectedSurv = evaluateSurvival(currentAge, estimatedSurvival, doLevel, tanLevel, ph).expectedSurvival
        val survDev = expectedSurv - estimatedSurvival
        val survScore = when {
            survDev > 15 -> 30
            survDev > 5  -> 15
            survDev > 0  -> 5
            else         -> 0
        }
        if (survScore > 0) factors.add("Survival ${String.format("%.0f", survDev)}% below target")
        score += survScore

        val tempDev = abs(temp - 28.0)
        val tempScore = when {
            tempDev > 6 -> 20
            tempDev > 3 -> 10
            tempDev > 1 -> 5
            else -> 0
        }
        if (tempScore > 0) factors.add("Temp deviation: ${temp}°C (optimal 28°C)")
        score += tempScore

        val level = when {
            score >= 60 -> "HIGH"
            score >= 35 -> "MODERATE"
            else -> "LOW"
        }

        return DiseaseRisk(score.coerceIn(0, 100), level, factors)
    }

    // ----------------------------------------------------
    // Module 10: Offline Rule-Based Advice (T3.5)
    // ----------------------------------------------------
    fun generateOfflineAdvice(cycle: PondCycle): String {
        val cost = evaluateCosts(
            pondSize = cycle.pondSize, proposedDensity = cycle.proposedDensity,
            estimatedSurvival = cycle.estimatedSurvival, currentAbw = cycle.currentAbw,
            age = cycle.currentAge, totalFeed = cycle.totalFeedConsumed,
            plUnitCost = cycle.plUnitCost, feedCostPerKg = cycle.feedCostPerKg,
            aerationCost = cycle.aerationCostPerDay, probioticCost = cycle.probioticCostPerDay,
            laborCost = cycle.laborCostPerDay
        )
        val surv = evaluateSurvival(cycle.currentAge, cycle.estimatedSurvival, cycle.doLevel, cycle.tanLevel, cycle.ph)
        val fcrRating = when {
            cost.fcr < 1.1  -> "unusually low — verify stock counts"
            cost.fcr < 1.5  -> "excellent"
            cost.fcr < 1.8  -> "elevated — review feeding schedule"
            else            -> "critical — immediate action required"
        }
        return buildString {
            appendLine("[Offline Mode] AI advisor is unavailable. Rule-based summary:")
            appendLine()
            appendLine("Survival: ${String.format("%.1f", cycle.estimatedSurvival)}% (expected ${String.format("%.1f", surv.expectedSurvival)}%) — ${surv.classification}")
            appendLine("FCR: ${String.format("%.2f", cost.fcr)} — $fcrRating")
            appendLine("Cost/kg: \$${String.format("%.2f", cost.costPerKg)}")
            appendLine("Biomass: ${String.format("%.0f", cost.currentBiomass)} kg")
            if (surv.status != SurvivalStatus.GREEN) {
                appendLine()
                appendLine("Action: ${surv.actionSteps.firstOrNull() ?: "Monitor water quality closely."}")
            }
        }.trim()
    }
}
