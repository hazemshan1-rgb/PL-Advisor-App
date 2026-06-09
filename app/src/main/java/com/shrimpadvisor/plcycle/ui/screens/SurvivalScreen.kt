package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors
import com.shrimpadvisor.plcycle.ui.SurvivalCurveChart
import com.shrimpadvisor.plcycle.ui.WaterQualityTrendChart

/**
 * Screen 3: Survival Trajectory Monitor
 */
@Composable
fun SurvivalMonitorTab(
    cycle: PondCycle,
    result: AdvisorEngine.SurvivalTrajectoryResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit,
    dailyReadings: List<DailyReading> = emptyList(),
    onLogReading: (Double) -> Unit = {}
) {
    var feedGivenInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("survival_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Survival Trajectory & Diagnostics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Compares sampling count parameters and isolates environmental/pathogenic factors.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.currentAge.let { if (it == 0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 1
                            onUpdate { current -> current.copy(currentAge = parsed) }
                        },
                        label = { Text("Pond Age (Days)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_age"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.estimatedSurvival.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(estimatedSurvival = parsed) }
                        },
                        label = { Text("Est. Survival %", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_survival"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Log today's reading — with optional feed given field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${dailyReadings.size} reading(s) logged",
                fontSize = 12.sp,
                color = AquaticColors.SoftMutedText,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = feedGivenInput,
                onValueChange = { feedGivenInput = it },
                label = { Text("Feed given (kg)", fontSize = 10.sp) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(120.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )
            OutlinedButton(onClick = {
                val feedKg = feedGivenInput.toDoubleOrNull() ?: 0.0
                onLogReading(feedKg)
                feedGivenInput = ""
            }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Curve Graph
        if (result != null) {
            Text("Survival Deviation Graphic", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            SurvivalCurveChart(
                currentDay = cycle.currentAge,
                estimatedSurvival = cycle.estimatedSurvival,
                expectedSurvival = result.expectedSurvival,
                historicalReadings = dailyReadings
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Water quality trend chart — only rendered when ≥2 readings are available
            if (dailyReadings.size >= 2) {
                WaterQualityTrendChart(readings = dailyReadings)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Diagnostic outputs
            val titleColor = when (result.status) {
                AdvisorEngine.SurvivalStatus.GREEN -> AquaticColors.SafeGreen
                AdvisorEngine.SurvivalStatus.YELLOW -> AquaticColors.SandGold
                AdvisorEngine.SurvivalStatus.RED -> AquaticColors.AlarmRed
            }
            val borderBg = titleColor.copy(alpha = 0.08f)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = borderBg),
                border = BorderStroke(1.dp, titleColor.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Classification: " + result.classification,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Deviation: ${String.format("%+.1f%%", result.deviation)} against reference limit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Symptoms Diagnostic:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    result.diagnostics.forEach { diag ->
                        Text("• $diag", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Directed Interventions Required:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    result.actionSteps.forEach { step ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("⚡ ", fontSize = 11.sp)
                            Text(step, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
