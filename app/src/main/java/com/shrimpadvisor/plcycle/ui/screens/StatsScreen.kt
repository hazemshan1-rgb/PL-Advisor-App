package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors

/**
 * Screen 10: Performance Benchmarking
 */
@Composable
fun PerformanceBenchmarkTab(cycles: List<PondCycle>) {
    data class BenchRow(val name: String, val doc: Int, val survival: Double, val fcr: Double, val costPerKg: Double)

    val rows = remember(cycles) {
        cycles.map { c ->
            val cost = AdvisorEngine.evaluateCosts(
                pondSize = c.pondSize, proposedDensity = c.proposedDensity,
                estimatedSurvival = c.estimatedSurvival, currentAbw = c.currentAbw,
                age = c.currentAge, totalFeed = c.totalFeedConsumed,
                plUnitCost = c.plUnitCost, feedCostPerKg = c.feedCostPerKg,
                aerationCost = c.aerationCostPerDay, probioticCost = c.probioticCostPerDay,
                laborCost = c.laborCostPerDay
            )
            BenchRow(c.pondName, c.currentAge, c.estimatedSurvival, cost.fcr, cost.costPerKg)
        }.sortedByDescending { it.survival }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("Performance Benchmarks", fontSize = 16.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("All ponds ranked by survival — highest first",
            fontSize = 11.sp, color = AquaticColors.SoftMutedText)

        Spacer(modifier = Modifier.height(12.dp))

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pond data available.", color = AquaticColors.SoftMutedText)
            }
        } else {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                listOf("Pond", "DOC", "Surv%", "FCR", "$/kg").forEach { h ->
                    Text(h, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(rows) { row ->
                    val fcrColor = when {
                        row.fcr < 1.5  -> AquaticColors.SafeGreen
                        row.fcr < 1.8  -> AquaticColors.SandGold
                        else           -> AquaticColors.AlarmRed
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.name.take(10), fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("${row.doc}", fontSize = 11.sp,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("${String.format("%.0f", row.survival)}%", fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = AquaticColors.SafeGreen,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(String.format("%.2f", row.fcr), fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = fcrColor,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(String.format("$%.2f", row.costPerKg), fontSize = 11.sp,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
