package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors
import com.shrimpadvisor.plcycle.ui.SummaryCard
import com.shrimpadvisor.plcycle.ui.FeedingReminderCard

/**
 * Screen 0: Overview Dashboard Card-Deck
 */
@Composable
fun DashboardOverviewTab(
    cycle: PondCycle,
    plResult: AdvisorEngine.QualityResult?,
    stocking: AdvisorEngine.StockingResult?,
    survival: AdvisorEngine.SurvivalTrajectoryResult?,
    cost: AdvisorEngine.CostTrackingResult?,
    harvest: AdvisorEngine.HarvestOptimizerResult?,
    feedRecommendation: AdvisorEngine.FeedRecommendation? = null,
    survivalForecast: AdvisorEngine.SurvivalForecast? = null,
    diseaseRisk: AdvisorEngine.DiseaseRisk? = null,
    onNavigateToTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("dashboard_overview_tab"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cycle Progress Card (Active Growth Card)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Decorative circular shape representing water flow/ponds
                    val circleColor = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.size(120.dp).align(Alignment.TopEnd)) {
                        drawCircle(
                            color = circleColor,
                            alpha = 0.05f,
                            radius = size.width / 2,
                            center = Offset(size.width * 1.1f, -size.height * 0.1f)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE GROWTH",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Vannamei • Cycle ${String.format("%02d", cycle.id % 10 + 1)}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Day ${cycle.currentAge} ",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = "Projected Harvest Size: ${cycle.harvestWeightTarget}g",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "EST. BIOMASS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = harvest?.currentBiomass?.let { String.format("%,.0f kg", it) } ?: "--- kg",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Small decorative bar equalizers on the right side
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf(12, 24, 16, 32, 20).forEach { hDp ->
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(hDp.dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Module summary block grid
        item {
            Text("Module Health Scorecard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Mod 1: Quality Gate Card
        item {
            SummaryCard(
                title = "1. PL Quality Gate Score",
                statusText = plResult?.statusMessage ?: "Awaiting parameters",
                statusColor = when (plResult?.verdict) {
                    AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen
                    AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold
                    AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed
                    null -> AquaticColors.SoftMutedText
                },
                scoreText = plResult?.score?.let { String.format("%.0f%%", it) } ?: "--",
                icon = Icons.Default.VerifiedUser,
                onClick = { onNavigateToTab(1) }
            )
        }

        // Mod 2: Carrying Capacity Card
        item {
            val exceed = stocking?.carryingCapacityExceeded ?: false
            val capColor = if (exceed) AquaticColors.AlarmRed else AquaticColors.SafeGreen
            val capMsg = if (exceed) "DANGER: Carrying Capacity Exceeded!" else "Bio-load within safe carrying threshold."

            SummaryCard(
                title = "2. Stocking Safe Loading",
                statusText = capMsg,
                statusColor = capColor,
                scoreText = stocking?.proposedTotalQty?.let { String.format("%,.0f PL", it) } ?: "--",
                icon = Icons.Default.WaterDrop,
                onClick = { onNavigateToTab(2) }
            )
        }

        // Mod 3: Survival Trajectory Card
        item {
            SummaryCard(
                title = "3. Survival Trajectory",
                statusText = when (survival?.status) {
                    AdvisorEngine.SurvivalStatus.GREEN -> "GREEN: Excellent survival trajectory."
                    AdvisorEngine.SurvivalStatus.YELLOW -> "YELLOW: Caution. Deviation from expected curve."
                    AdvisorEngine.SurvivalStatus.RED -> "RED: Critical. Immediate remediation REQUIRED."
                    null -> "--"
                },
                statusColor = when (survival?.status) {
                    AdvisorEngine.SurvivalStatus.GREEN -> AquaticColors.SafeGreen
                    AdvisorEngine.SurvivalStatus.YELLOW -> AquaticColors.SandGold
                    AdvisorEngine.SurvivalStatus.RED -> AquaticColors.AlarmRed
                    null -> AquaticColors.SoftMutedText
                },
                scoreText = survival?.actualSurvival?.let { String.format("%.0f%%", it) } ?: "--",
                icon = Icons.Default.Analytics,
                onClick = { onNavigateToTab(3) }
            )
        }

        // Mod 4: Cost & FCR Card
        item {
            SummaryCard(
                title = "4. Food FCR & Debt Accumulation",
                statusText = cost?.fcrStatusMessage ?: "--",
                statusColor = when (cost?.fcrStatusColor) {
                    "success" -> AquaticColors.SafeGreen
                    "warning" -> AquaticColors.SandGold
                    "critical" -> AquaticColors.AlarmRed
                    else -> AquaticColors.SoftMutedText
                },
                scoreText = cost?.fcr?.let { String.format("FCR %.2f", it) } ?: "--",
                icon = Icons.Default.Payments,
                onClick = { onNavigateToTab(4) }
            )
        }

        // Mod 5: Harvest Optimizer Card
        item {
            val rec = if (harvest?.shouldHarvestNow == true) "HARVEST NOW: Reaching plateau." else "HOLD GROWING: Profitable holding window active."
            val optColor = if (harvest?.shouldHarvestNow == true) AquaticColors.SafeGreen else AquaticColors.ElectricTeal

            SummaryCard(
                title = "5. Harvest Window Optimizer",
                statusText = rec,
                statusColor = optColor,
                scoreText = harvest?.bestHoldScenario?.let { "Hold ${it.day}d" } ?: "Now",
                icon = Icons.Default.TrendingUp,
                onClick = { onNavigateToTab(5) }
            )
        }

        // T1.1 — Feed Recommendation
        feedRecommendation?.let { rec ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AquaticColors.ElectricTeal.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AquaticColors.ElectricTeal.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null,
                            tint = AquaticColors.ElectricTeal, modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Today's Feed Recommendation", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = AquaticColors.ElectricTeal)
                            Text(rec.adjustmentNote, fontSize = 11.sp, color = AquaticColors.SoftMutedText)
                        }
                        Text(
                            String.format("%.1f kg", rec.recommendedKgPerDay),
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                            color = AquaticColors.ElectricTeal
                        )
                    }
                }
            }
        }

        // T1.2 — Survival Forecast Alert
        survivalForecast?.let { fc ->
            if (fc.isAlert) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AquaticColors.AlarmRed.copy(alpha = 0.08f)),
                        border = BorderStroke(1.5.dp, AquaticColors.AlarmRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null,
                                tint = AquaticColors.AlarmRed, modifier = Modifier.size(22.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Survival Forecast Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = AquaticColors.AlarmRed)
                                Text("Trend: ${fc.trend}", fontSize = 11.sp, color = AquaticColors.SoftMutedText)
                                Text(
                                    "3-day: ${String.format("%.1f", fc.forecastDay3)}%  |  7-day: ${String.format("%.1f", fc.forecastDay7)}%",
                                    fontSize = 11.sp, color = AquaticColors.AlarmRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // T2.3 — Disease Risk Score
        diseaseRisk?.let { risk ->
            item {
                val riskColor = when (risk.level) {
                    "HIGH" -> AquaticColors.AlarmRed
                    "MODERATE" -> AquaticColors.SandGold
                    else -> AquaticColors.SafeGreen
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null,
                            tint = riskColor, modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Disease Risk Index", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = riskColor)
                            if (risk.factors.isNotEmpty()) {
                                Text(risk.factors.joinToString(" · "), fontSize = 10.sp,
                                    color = AquaticColors.SoftMutedText, lineHeight = 14.sp)
                            } else {
                                Text("All parameters within safe range", fontSize = 11.sp, color = AquaticColors.SoftMutedText)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${risk.score}/100", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = riskColor)
                            Text(risk.level, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = riskColor)
                        }
                    }
                }
            }
        }

        item {
            FeedingReminderCard(pondName = cycle.pondName)
        }
    }
}
