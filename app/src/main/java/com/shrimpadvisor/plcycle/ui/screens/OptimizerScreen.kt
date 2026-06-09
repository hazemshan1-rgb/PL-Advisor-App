package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.data.RegionProfile
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors
import com.shrimpadvisor.plcycle.ui.HarvestCalendarStrip
import com.shrimpadvisor.plcycle.ui.ProfitScenarioBarChart
import com.shrimpadvisor.plcycle.ui.RegionSelectorCard

/**
 * Screen 5: Harvest Window Optimizer
 */
@Composable
fun HarvestOptimizerTab(
    cycle: PondCycle,
    result: AdvisorEngine.HarvestOptimizerResult?,
    regionProfiles: List<RegionProfile> = emptyList(),
    activeRegionProfile: RegionProfile? = null,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit,
    onLinkRegionProfile: (Int?) -> Unit = {},
    onSaveRegionProfile: (RegionProfile) -> Unit = {},
    onDeleteRegionProfile: (RegionProfile) -> Unit = {}
) {
    var showDiseaseScenario by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("optimizer_tab")
    ) {
        // ── Region Selector Card ──────────────────────────────────────────────
        RegionSelectorCard(
            regionProfiles = regionProfiles,
            activeRegionProfile = activeRegionProfile,
            onSelectProfile = { selected ->
                onLinkRegionProfile(selected?.id)
            },
            onSaveProfile = onSaveRegionProfile,
            onDeleteProfile = onDeleteRegionProfile
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Harvest Window Opt. Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Simulates 30-day future feeding schedules and evaluates size-market premiums to find highest peak profits.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.averageDailyGain.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(averageDailyGain = parsed) }
                        },
                        label = { Text("Avg Daily Gain (g/day)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_adg"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.marketPriceTarget.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(marketPriceTarget = parsed) }
                        },
                        label = { Text("Market Price Target ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_market_price"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Mortality Settings Card ──────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.testTag("mortality_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Mortality Settings",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Configure baseline daily mortality and weekly acceleration for the harvest optimizer.",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mortality rate slider: 0.001 (0.1%) – 0.020 (2.0%), step 0.001
                val mortalitySteps = 19 // steps between 0.1% and 2.0% at 0.1% increments
                val mortalitySliderValue = ((cycle.customMortalityRate.coerceIn(0.001, 0.020) - 0.001) / 0.001).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Mortality Rate", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        String.format("%.1f%%/day", cycle.customMortalityRate * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = mortalitySliderValue,
                    onValueChange = { raw ->
                        val stepped = raw.toInt().coerceIn(0, mortalitySteps)
                        val newRate = 0.001 + stepped * 0.001
                        onUpdate { current -> current.copy(customMortalityRate = newRate) }
                    },
                    valueRange = 0f..mortalitySteps.toFloat(),
                    steps = mortalitySteps - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_mortality_rate"),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.1%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                    Text("2.0%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Acceleration slider: 0.0 – 0.002 (0.0 – 0.2%/week), step 0.0001
                val accelSteps = 20 // steps 0.0 to 0.002 at 0.0001 increments
                val accelSliderValue = ((cycle.mortalityAcceleration.coerceIn(0.0, 0.002) / 0.0001)).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mortality Acceleration", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        String.format("+%.2f%%/wk", cycle.mortalityAcceleration * 100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Slider(
                    value = accelSliderValue,
                    onValueChange = { raw ->
                        val stepped = raw.toInt().coerceIn(0, accelSteps)
                        val newAccel = stepped * 0.0001
                        onUpdate { current -> current.copy(mortalityAcceleration = newAccel) }
                    },
                    valueRange = 0f..accelSteps.toFloat(),
                    steps = accelSteps - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_mortality_acceleration"),
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.00%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                    Text("0.20%", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Disease Scenario toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Disease Scenario", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "If disease hits (×2.5 mortality), compares projected best harvest day and revenue delta.",
                            fontSize = 10.sp,
                            color = AquaticColors.SoftMutedText
                        )
                    }
                    Switch(
                        checked = showDiseaseScenario,
                        onCheckedChange = { showDiseaseScenario = it },
                        modifier = Modifier.testTag("toggle_disease_scenario")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result != null) {
            // T2.4 — Harvest Calendar
            Text(
                "Harvest Timeline",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            HarvestCalendarStrip(
                stockingDate = cycle.stockingDate,
                currentAge = cycle.currentAge,
                optimalHoldDay = result.bestHoldScenario?.day,
                shouldHarvestNow = result.shouldHarvestNow
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("30-Day Profit Projection Trend", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            ProfitScenarioBarChart(
                scenarios = result.holdScenariosList,
                bestHoldDay = result.bestHoldScenario?.day
            )

            Spacer(modifier = Modifier.height(16.dp))

            val optimizerBg = if (result.shouldHarvestNow) AquaticColors.SafeGreen.copy(alpha = 0.1f) else AquaticColors.ElectricTeal.copy(alpha = 0.08f)
            val borderClr = if (result.shouldHarvestNow) AquaticColors.SafeGreen else AquaticColors.ElectricTeal

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = optimizerBg),
                border = BorderStroke(1.dp, borderClr.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (result.shouldHarvestNow) Icons.Default.DoneOutline else Icons.Default.TrendingFlat,
                            contentDescription = null,
                            tint = borderClr
                        )
                        Text(
                            text = if (result.shouldHarvestNow) "RECOMMENDATION: HARVEST NOW" else "RECOMMENDATION: HOLD FOR MORE DAYS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = borderClr
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (result.shouldHarvestNow) {
                            "Shrimp biological mass curves indicate growth deceleration. Daily feed maintenance overhead exceeds size-based market price premiums. Promptly harvest and reseed."
                        } else {
                            "Hold stocking with expected ADG (${cycle.averageDailyGain}g/day) for ${result.bestHoldScenario?.day} more days to maximize cash outcomes. Projected net premium: ${String.format("$%,.2f", result.profitDifferential)}"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }

            // ── Disease Scenario Card ─────────────────────────────────────────
            if (showDiseaseScenario) {
                val diseaseResult = result.scenarioResult
                if (diseaseResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val diseaseBg = Color(0xFFFF5722).copy(alpha = 0.08f)
                    val diseaseBorder = Color(0xFFFF5722)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = diseaseBg),
                        border = BorderStroke(1.dp, diseaseBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("disease_scenario_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = diseaseBorder
                                )
                                Text(
                                    "If Disease Hits (×2.5 Mortality)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = diseaseBorder
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val diseaseBestDay = diseaseResult.bestHoldScenario?.day
                            val normalBestDay = result.bestHoldScenario?.day
                            val revenueDelta = diseaseResult.profitDifferential - result.profitDifferential

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Best Harvest Day", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                                    Text(
                                        if (diseaseResult.shouldHarvestNow) "Harvest Now" else "Day +${diseaseBestDay}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = diseaseBorder
                                    )
                                    if (normalBestDay != null && diseaseBestDay != null && diseaseBestDay != normalBestDay) {
                                        Text(
                                            "(normal: Day +${normalBestDay})",
                                            fontSize = 10.sp,
                                            color = AquaticColors.SoftMutedText
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Revenue Delta vs. Normal", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                                    Text(
                                        (if (revenueDelta >= 0) "+" else "") + String.format("$%,.2f", revenueDelta),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (revenueDelta >= 0) AquaticColors.SafeGreen else diseaseBorder
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Applied mortality rate: ${String.format("%.1f%%/day", diseaseResult.mortalityRatePerDay * 100)} (baseline ×2.5). Harvest earlier to minimize disease-driven crop losses.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
