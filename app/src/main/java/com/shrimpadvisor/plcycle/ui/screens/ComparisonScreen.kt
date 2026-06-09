package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.data.RegionProfile
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors

/**
 * Screen 9: Multi-Pond Comparison
 */
@Composable
fun PondComparisonTab(
    cycles: List<PondCycle>,
    allRegionProfiles: List<RegionProfile>
) {
    data class PondSnapshot(
        val cycle: PondCycle,
        val survivalStatus: AdvisorEngine.SurvivalStatus,
        val fcr: Double,
        val costPerKg: Double,
        val biomass: Double
    )

    val snapshots = remember(cycles, allRegionProfiles) {
        cycles.map { c ->
            val profile = allRegionProfiles.firstOrNull { it.id == c.regionProfileId }
            val cost = AdvisorEngine.evaluateCosts(
                pondSize = c.pondSize, proposedDensity = c.proposedDensity,
                estimatedSurvival = c.estimatedSurvival, currentAbw = c.currentAbw,
                age = c.currentAge, totalFeed = c.totalFeedConsumed,
                plUnitCost = c.plUnitCost,
                feedCostPerKg = profile?.feedCostDefault ?: c.feedCostPerKg,
                aerationCost = profile?.aerationCostDefault ?: c.aerationCostPerDay,
                probioticCost = profile?.probioticCostDefault ?: c.probioticCostPerDay,
                laborCost = profile?.laborCostDefault ?: c.laborCostPerDay
            )
            val surv = AdvisorEngine.evaluateSurvival(c.currentAge, c.estimatedSurvival,
                c.doLevel, c.tanLevel, c.ph)
            PondSnapshot(c, surv.status, cost.fcr, cost.costPerKg, cost.currentBiomass)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("Pond Comparison", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Side-by-side health & economics across all active ponds",
            fontSize = 11.sp, color = AquaticColors.SoftMutedText)

        Spacer(modifier = Modifier.height(12.dp))

        if (snapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ponds available.", color = AquaticColors.SoftMutedText)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(snapshots, key = { it.cycle.id }) { snap ->
                    val survColor = when (snap.survivalStatus) {
                        AdvisorEngine.SurvivalStatus.GREEN  -> AquaticColors.SafeGreen
                        AdvisorEngine.SurvivalStatus.YELLOW -> AquaticColors.SandGold
                        AdvisorEngine.SurvivalStatus.RED    -> AquaticColors.AlarmRed
                    }
                    val fcrColor = when {
                        snap.fcr < 1.5  -> AquaticColors.SafeGreen
                        snap.fcr < 1.8  -> AquaticColors.SandGold
                        else            -> AquaticColors.AlarmRed
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(snap.cycle.pondName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("DOC ${snap.cycle.currentAge}  ·  ${String.format("%.0f", snap.cycle.pondSize)} m²",
                                        fontSize = 11.sp, color = AquaticColors.SoftMutedText)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(survColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(snap.survivalStatus.name, fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, color = survColor)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                CompareMetric("Survival",
                                    "${String.format("%.0f", snap.cycle.estimatedSurvival)}%", survColor)
                                CompareMetric("FCR", String.format("%.2f", snap.fcr), fcrColor)
                                CompareMetric("$/kg", String.format("$%.2f", snap.costPerKg),
                                    AquaticColors.ElectricTeal)
                                CompareMetric("Biomass", "${String.format("%.0f", snap.biomass)}kg",
                                    MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 9.sp, color = AquaticColors.SoftMutedText, fontWeight = FontWeight.Bold)
    }
}
