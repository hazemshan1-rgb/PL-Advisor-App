package com.shrimpadvisor.plcycle.ui

import com.shrimpadvisor.plcycle.data.RegionProfile
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
        mortalityRatePerDay: Double = 0.004,
        mortalityAcceleration: Double = 0.0,
        regionProfile: RegionProfile? = null
    ): HarvestOptimizerResult {
        val initialStockingQty = proposedDensity * pondSize
        val currentShrimpQty = initialStockingQty * (estimatedSurvival / 100.0)

        val currentBiomass = max(1.0, currentShrimpQty * (currentAbw / 1000.0))
        val currentPrice = getPricePerKg(currentAbw, regionProfile)
        val currentRevenue = currentBiomass * currentPrice

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

                val dailyFeedCost = projectedBiomass * 0.025 * feedCostPerKg
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
}
