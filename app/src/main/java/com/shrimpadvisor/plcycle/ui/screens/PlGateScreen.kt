package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors

/**
 * Screen 1: PL Pre-Stocking Quality Gate
 */
@Composable
fun PlGateTab(
    cycle: PondCycle,
    result: AdvisorEngine.QualityResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("pl_gate_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pre-Stocking Quality Gate", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Grades baby shrimp health parameters prior to release inside ponds.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                // Score 1: Stress Tolerance
                ScoreSliderRow(
                    label = "Stress Tolerance Test",
                    value = cycle.stressToleranceScore,
                    description = "Survival score in high saline / formalin shock tests.",
                    onValueChange = { score -> onUpdate { it.copy(stressToleranceScore = score) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Score 2: Gut Fullness
                ScoreSliderRow(
                    label = "Gut Fullness & Lipid Ratio",
                    value = cycle.gutFullnessScore,
                    description = "Percentage of PLs showing full guts and high lipids under scope.",
                    onValueChange = { score -> onUpdate { it.copy(gutFullnessScore = score) } }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Score 3: Supplier Integrity
                ScoreSliderRow(
                    label = "Hatchery Historical Score",
                    value = cycle.supplierScore,
                    description = "Historical SPF disease-free verification record of this hatchery.",
                    onValueChange = { score -> onUpdate { it.copy(supplierScore = score) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Verdict
        if (result != null) {
            val panelBg = when (result.verdict) {
                AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen.copy(alpha = 0.08f)
                AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold.copy(alpha = 0.12f)
                AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed.copy(alpha = 0.08f)
            }
            val titleColor = when (result.verdict) {
                AdvisorEngine.QualityVerdict.STOCK -> AquaticColors.SafeGreen
                AdvisorEngine.QualityVerdict.HOLD -> AquaticColors.SandGold
                AdvisorEngine.QualityVerdict.REJECT -> AquaticColors.AlarmRed
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = panelBg),
                border = BorderStroke(1.dp, titleColor.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (result.verdict) {
                                AdvisorEngine.QualityVerdict.STOCK -> Icons.Default.CheckCircle
                                AdvisorEngine.QualityVerdict.HOLD -> Icons.Default.PauseCircle
                                AdvisorEngine.QualityVerdict.REJECT -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = titleColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = result.statusMessage,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = titleColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Cycle Recommendations:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    result.recommendations.forEach { recommendation ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
                            Text(recommendation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreSliderRow(
    label: String,
    value: Int,
    description: String,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(description, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
            Text(
                text = "$value%",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}
