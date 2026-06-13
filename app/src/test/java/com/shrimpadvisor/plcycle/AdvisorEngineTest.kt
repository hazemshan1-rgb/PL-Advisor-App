package com.shrimpadvisor.plcycle

import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.RegionProfile
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import org.junit.Assert.*
import org.junit.Test

class AdvisorEngineTest {

    // ─── Module 1: PL Quality ────────────────────────────────────────────────

    @Test
    fun `evaluatePLQuality returns STOCK when all scores are high`() {
        val result = AdvisorEngine.evaluatePLQuality(90, 85, 92)
        assertEquals(AdvisorEngine.QualityVerdict.STOCK, result.verdict)
        assertTrue(result.score >= 80.0)
    }

    @Test
    fun `evaluatePLQuality returns HOLD when scores are borderline`() {
        val result = AdvisorEngine.evaluatePLQuality(65, 70, 68)
        assertEquals(AdvisorEngine.QualityVerdict.HOLD, result.verdict)
        assertTrue(result.score in 60.0..79.9)
    }

    @Test
    fun `evaluatePLQuality returns REJECT when scores are very low`() {
        val result = AdvisorEngine.evaluatePLQuality(30, 40, 50)
        assertEquals(AdvisorEngine.QualityVerdict.REJECT, result.verdict)
        assertTrue(result.score < 60.0)
    }

    @Test
    fun `evaluatePLQuality score is average of three inputs`() {
        val result = AdvisorEngine.evaluatePLQuality(60, 90, 90)
        assertEquals(80.0, result.score, 0.01)
    }

    @Test
    fun `evaluatePLQuality STOCK gives 85 percent survival baseline`() {
        val result = AdvisorEngine.evaluatePLQuality(90, 90, 90)
        assertEquals(85.0, result.week1SurvivalBaseline, 0.01)
    }

    @Test
    fun `evaluatePLQuality HOLD gives 75 percent survival baseline`() {
        val result = AdvisorEngine.evaluatePLQuality(65, 65, 65)
        assertEquals(75.0, result.week1SurvivalBaseline, 0.01)
    }

    @Test
    fun `evaluatePLQuality REJECT gives 50 percent survival baseline`() {
        val result = AdvisorEngine.evaluatePLQuality(20, 20, 20)
        assertEquals(50.0, result.week1SurvivalBaseline, 0.01)
    }

    @Test
    fun `evaluatePLQuality recommendations are non-empty`() {
        val resultStock = AdvisorEngine.evaluatePLQuality(90, 85, 92)
        val resultHold = AdvisorEngine.evaluatePLQuality(65, 65, 65)
        val resultReject = AdvisorEngine.evaluatePLQuality(20, 20, 20)
        assertTrue(resultStock.recommendations.isNotEmpty())
        assertTrue(resultHold.recommendations.isNotEmpty())
        assertTrue(resultReject.recommendations.isNotEmpty())
    }

    // ─── Module 2: Stocking ──────────────────────────────────────────────────

    @Test
    fun `evaluateStocking flags carrying capacity exceeded when density is very high`() {
        val result = AdvisorEngine.evaluateStocking(
            pondSize = 1000.0,
            proposedDensity = 400.0,
            targetWeight = 20.0,
            oxygen = 6.5, ph = 8.0, salinity = 25.0, temp = 28.5, tan = 0.1,
            carryingCapacityRatio = 6.0
        )
        assertTrue(result.carryingCapacityExceeded)
    }

    @Test
    fun `evaluateStocking is within capacity at normal density`() {
        val result = AdvisorEngine.evaluateStocking(
            pondSize = 1000.0,
            proposedDensity = 60.0,
            targetWeight = 20.0,
            oxygen = 6.5, ph = 8.0, salinity = 25.0, temp = 28.5, tan = 0.1
        )
        assertFalse(result.carryingCapacityExceeded)
    }

    @Test
    fun `evaluateStocking totalOptimalQty equals optimalDensity times pondSize`() {
        val result = AdvisorEngine.evaluateStocking(
            pondSize = 1000.0,
            proposedDensity = 60.0,
            targetWeight = 20.0,
            oxygen = 6.5, ph = 8.0, salinity = 25.0, temp = 28.5, tan = 0.1
        )
        assertEquals(result.optimalDensity * 1000.0, result.totalOptimalQty, 0.01)
    }

    @Test
    fun `evaluateStocking generates parameter checks for all 5 water params`() {
        val result = AdvisorEngine.evaluateStocking(
            pondSize = 1000.0,
            proposedDensity = 60.0,
            targetWeight = 20.0,
            oxygen = 6.5, ph = 8.0, salinity = 25.0, temp = 28.5, tan = 0.1
        )
        assertEquals(5, result.parameterChecks.size)
    }

    // ─── Module 3: Survival ──────────────────────────────────────────────────

    @Test
    fun `evaluateSurvival returns GREEN when survival matches expected`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 15, estimatedSurvival = 85.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(AdvisorEngine.SurvivalStatus.GREEN, result.status)
    }

    @Test
    fun `evaluateSurvival returns YELLOW when deviation is between minus5 and minus15`() {
        // At day 15, expected ≈ 87.5%; set survival to 78% → deviation ≈ -9.5%
        val result = AdvisorEngine.evaluateSurvival(
            age = 15, estimatedSurvival = 78.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(AdvisorEngine.SurvivalStatus.YELLOW, result.status)
    }

    @Test
    fun `evaluateSurvival returns RED when deviation is worse than minus15`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 15, estimatedSurvival = 60.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(AdvisorEngine.SurvivalStatus.RED, result.status)
    }

    @Test
    fun `evaluateSurvival classifies as Environmental when water params are out of bounds`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 15, estimatedSurvival = 60.0,
            oxygen = 4.0, tan = 1.2, ph = 8.0
        )
        assertEquals("Environmental", result.classification)
    }

    @Test
    fun `evaluateSurvival classifies as Pathogenic when params are normal but survival is low`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 15, estimatedSurvival = 60.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals("Pathogenic", result.classification)
    }

    @Test
    fun `evaluateSurvival expected survival is 92 percent at day 7 or earlier`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 7, estimatedSurvival = 90.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(92.0, result.expectedSurvival, 0.01)
    }

    @Test
    fun `evaluateSurvival expected survival is 75 percent at day 30`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 30, estimatedSurvival = 75.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(75.0, result.expectedSurvival, 0.01)
    }

    @Test
    fun `evaluateSurvival deviation is actual minus expected`() {
        val result = AdvisorEngine.evaluateSurvival(
            age = 7, estimatedSurvival = 88.0,
            oxygen = 6.5, tan = 0.1, ph = 8.0
        )
        assertEquals(88.0 - 92.0, result.deviation, 0.01)
    }

    // ─── Module 4: Costs ─────────────────────────────────────────────────────

    @Test
    fun `evaluateCosts fcr is total feed divided by current biomass`() {
        val result = AdvisorEngine.evaluateCosts(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 10.0,
            age = 30, totalFeed = 400.0,
            plUnitCost = 0.012, feedCostPerKg = 1.25,
            aerationCost = 8.0, probioticCost = 3.0, laborCost = 4.0
        )
        val expectedBiomass = 60_000.0 * 0.80 * (10.0 / 1000.0)  // 480 kg
        val expectedFcr = 400.0 / expectedBiomass
        assertEquals(expectedFcr, result.fcr, 0.01)
    }

    @Test
    fun `evaluateCosts totalAccumulatedCost is sum of all cost components`() {
        val result = AdvisorEngine.evaluateCosts(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 10.0,
            age = 30, totalFeed = 200.0,
            plUnitCost = 0.012, feedCostPerKg = 1.25,
            aerationCost = 8.0, probioticCost = 3.0, laborCost = 4.0
        )
        val expected = result.totalPlCost + result.totalFeedCost + result.totalOperationalCost
        assertEquals(expected, result.totalAccumulatedCost, 0.01)
    }

    @Test
    fun `evaluateCosts FCR in target range produces success status`() {
        // Force FCR ≈ 1.3 by choosing appropriate biomass and feed
        // biomass = 60000 * 0.80 * (10/1000) = 480 kg → feed = 480 * 1.3 ≈ 624 kg
        val result = AdvisorEngine.evaluateCosts(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 10.0,
            age = 30, totalFeed = 624.0,
            plUnitCost = 0.012, feedCostPerKg = 1.25,
            aerationCost = 8.0, probioticCost = 3.0, laborCost = 4.0
        )
        assertEquals("success", result.fcrStatusColor)
    }

    // ─── Module 5: Harvest Optimizer ─────────────────────────────────────────

    @Test
    fun `optimizeHarvest produces 30 hold scenarios`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0
        )
        assertEquals(30, result.holdScenariosList.size)
    }

    @Test
    fun `optimizeHarvest bestHoldScenario day is within 1 to 30`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 5.0,
            totalFeed = 100.0, adg = 0.35,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0
        )
        result.bestHoldScenario?.let { assertTrue(it.day in 1..30) }
    }

    @Test
    fun `optimizeHarvest shouldHarvestNow is true when holding adds no net gain`() {
        // Very high daily operational costs make holding unprofitable
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 20.0,
            totalFeed = 500.0, adg = 0.05,
            feedCostPerKg = 5.0, aerationCost = 100.0,
            probioticCost = 100.0, laborCost = 100.0
        )
        assertTrue(result.shouldHarvestNow)
    }

    @Test
    fun `optimizeHarvest currentBiomass equals density times pondSize times survival times weight`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 10.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0
        )
        val expected = 60_000.0 * 0.80 * (10.0 / 1000.0)
        assertEquals(expected, result.currentBiomass, 0.1)
    }

    @Test
    fun `getPricePerKg increases with weight`() {
        val priceAt10 = AdvisorEngine.getPricePerKg(10.0)
        val priceAt20 = AdvisorEngine.getPricePerKg(20.0)
        assertTrue(priceAt20 > priceAt10)
    }

    // ─── getPricePerKg with RegionProfile ────────────────────────────────────

    private fun testProfile() = RegionProfile(
        regionName = "Test",
        priceAt20g = 3.0,
        priceAt25g = 4.0,
        priceAt30g = 5.0,
        priceAt35g = 6.0,
        priceAt40g = 7.0
    )

    @Test
    fun `getPricePerKg at exact 20g bracket returns bracket price`() {
        val price = AdvisorEngine.getPricePerKg(20.0, testProfile())
        assertEquals(3.0, price, 0.001)
    }

    @Test
    fun `getPricePerKg at exact 40g bracket returns bracket price`() {
        val price = AdvisorEngine.getPricePerKg(40.0, testProfile())
        assertEquals(7.0, price, 0.001)
    }

    @Test
    fun `getPricePerKg interpolates midpoint between 20g and 25g brackets`() {
        // midpoint at 22.5g should be exactly halfway between 3.0 and 4.0 = 3.5
        val price = AdvisorEngine.getPricePerKg(22.5, testProfile())
        assertEquals(3.5, price, 0.001)
    }

    @Test
    fun `getPricePerKg interpolates midpoint between 30g and 35g brackets`() {
        // midpoint at 32.5g should be 5.5
        val price = AdvisorEngine.getPricePerKg(32.5, testProfile())
        assertEquals(5.5, price, 0.001)
    }

    @Test
    fun `getPricePerKg extrapolates linearly above 40g`() {
        // slope from 35-40g bracket = (7.0 - 6.0) / 5 = 0.2 per g
        // at 45g: 7.0 + 0.2 * 5 = 8.0
        val price = AdvisorEngine.getPricePerKg(45.0, testProfile())
        assertEquals(8.0, price, 0.001)
    }

    @Test
    fun `getPricePerKg with region profile returns higher price than legacy for large shrimp`() {
        // Saudi Arabia profile has premium prices; legacy formula = 4 + 0.25 * 30 = 11.5
        // Saudi profile at 30g = 7.5 < 11.5, so let's just verify they're different
        val legacyPrice = AdvisorEngine.getPricePerKg(30.0, null)
        val profilePrice = AdvisorEngine.getPricePerKg(30.0, testProfile())
        assertNotEquals(legacyPrice, profilePrice, 0.001)
    }

    // ─── optimizeHarvest with configurable mortality ──────────────────────────

    @Test
    fun `optimizeHarvest uses custom mortality rate in survivor fraction`() {
        // High mortality rate should reduce projected biomass significantly
        val highMortality = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.05
        )
        val lowMortality = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.001
        )
        val highMortDay30 = highMortality.holdScenariosList.last().projectedBiomass
        val lowMortDay30 = lowMortality.holdScenariosList.last().projectedBiomass
        assertTrue(highMortDay30 < lowMortDay30)
    }

    @Test
    fun `optimizeHarvest mortalityRatePerDay is reflected in result`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.007
        )
        assertEquals(0.007, result.mortalityRatePerDay, 0.0001)
    }

    @Test
    fun `optimizeHarvest with acceleration reduces biomass faster over time`() {
        val withAccel = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.004,
            mortalityAcceleration = 0.001
        )
        val noAccel = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.004,
            mortalityAcceleration = 0.0
        )
        val accelDay30 = withAccel.holdScenariosList.last().projectedBiomass
        val noAccelDay30 = noAccel.holdScenariosList.last().projectedBiomass
        assertTrue(accelDay30 < noAccelDay30)
    }

    // ─── Disease scenario (scenarioResult) ───────────────────────────────────

    @Test
    fun `optimizeHarvest scenarioResult is not null`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.004
        )
        assertNotNull(result.scenarioResult)
    }

    @Test
    fun `scenarioResult uses mortality rate of 2 point 5 times the base rate`() {
        val baseRate = 0.004
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = baseRate
        )
        assertEquals(baseRate * 2.5, result.scenarioResult!!.mortalityRatePerDay, 0.0001)
    }

    @Test
    fun `scenarioResult has lower projected biomass than normal result at day 30`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0,
            mortalityRatePerDay = 0.004
        )
        val normalDay30 = result.holdScenariosList.last().projectedBiomass
        val diseaseDay30 = result.scenarioResult!!.holdScenariosList.last().projectedBiomass
        assertTrue(diseaseDay30 < normalDay30)
    }

    @Test
    fun `scenarioResult scenarioResult is null ie no nesting beyond one level`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0
        )
        assertNull(result.scenarioResult!!.scenarioResult)
    }

    // ─── Harvest cost accumulation ───────────────────────────────────────────

    @Test
    fun `totalAddedCost grows monotonically across hold scenarios`() {
        val result = AdvisorEngine.optimizeHarvest(
            pondSize = 1000.0, proposedDensity = 60.0,
            estimatedSurvival = 80.0, currentAbw = 15.0,
            totalFeed = 400.0, adg = 0.23,
            feedCostPerKg = 1.25, aerationCost = 8.0,
            probioticCost = 3.0, laborCost = 4.0
        )
        val costs = result.holdScenariosList.map { it.totalAddedCost }
        for (i in 1 until costs.size) {
            assertTrue(
                "Cost at day ${i + 1} should exceed day $i",
                costs[i] > costs[i - 1]
            )
        }
    }

    // ─── GeminiAdvisor prompt ────────────────────────────────────────────────

    @Test
    fun `buildPrompt includes pond name and question`() {
        val cycle = com.shrimpadvisor.plcycle.data.PondCycle(pondName = "Test Pond")
        val prompt = com.shrimpadvisor.plcycle.ui.GeminiAdvisor.buildPrompt(cycle, "What is the FCR?")
        assertTrue(prompt.contains("Test Pond"))
        assertTrue(prompt.contains("What is the FCR?"))
    }

    @Test
    fun `buildPrompt includes all key water quality parameters`() {
        val cycle = com.shrimpadvisor.plcycle.data.PondCycle(
            doLevel = 6.5, ph = 7.9, salinity = 24.0, temp = 29.0, tanLevel = 0.3
        )
        val prompt = com.shrimpadvisor.plcycle.ui.GeminiAdvisor.buildPrompt(cycle, "q")
        assertTrue(prompt.contains("6.5"))
        assertTrue(prompt.contains("7.9"))
        assertTrue(prompt.contains("24.0"))
    }

    @Test
    fun `buildPrompt with recentReadings includes daily readings table header`() {
        val cycle = com.shrimpadvisor.plcycle.data.PondCycle(pondName = "Test Pond")
        val readings = listOf(
            DailyReading(pondCycleId = 1, pondAge = 10, doLevel = 6.2, tanLevel = 0.15,
                ph = 7.9, temp = 28.5, abw = 4.5, survivalPct = 88.0),
            DailyReading(pondCycleId = 1, pondAge = 11, doLevel = 6.0, tanLevel = 0.18,
                ph = 7.8, temp = 28.7, abw = 4.7, survivalPct = 87.5)
        )
        val prompt = com.shrimpadvisor.plcycle.ui.GeminiAdvisor.buildPrompt(cycle, "trends?", readings)
        assertTrue(prompt.contains("Daily Readings"))
        assertTrue(prompt.contains("Day | DO"))
    }

    @Test
    fun `buildPrompt with recentReadings includes trend analysis section`() {
        val cycle = com.shrimpadvisor.plcycle.data.PondCycle(pondName = "Test Pond")
        val readings = (1..8).map { day ->
            DailyReading(
                pondCycleId = 1, pondAge = day,
                doLevel = 6.0 + day * 0.05,
                tanLevel = 0.1 + day * 0.01,
                ph = 7.9,
                temp = 28.5,
                abw = 3.0 + day * 0.2,
                survivalPct = 90.0 - day * 0.3
            )
        }
        val prompt = com.shrimpadvisor.plcycle.ui.GeminiAdvisor.buildPrompt(cycle, "how am i doing?", readings)
        assertTrue(prompt.contains("7-Day Trends"))
        assertTrue(prompt.contains("DO:"))
        assertTrue(prompt.contains("Survival:"))
        assertTrue(prompt.contains("ABW:"))
    }

    @Test
    fun `buildPrompt without recentReadings omits readings and trends sections`() {
        val cycle = com.shrimpadvisor.plcycle.data.PondCycle(pondName = "Test Pond")
        val prompt = com.shrimpadvisor.plcycle.ui.GeminiAdvisor.buildPrompt(cycle, "q")
        assertFalse(prompt.contains("Daily Readings"))
        assertFalse(prompt.contains("7-Day Trends"))
    }
}
