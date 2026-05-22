package com.shrimpadvisor.plcycle

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
            proposedDensity = 200.0,
            targetWeight = 20.0,
            oxygen = 6.5, ph = 8.0, salinity = 25.0, temp = 28.5, tan = 0.1
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
}
