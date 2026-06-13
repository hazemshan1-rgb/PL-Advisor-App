package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AquaticColors

@Composable
fun SettingsTab(
    cycle: PondCycle,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Text(
            "Agronomic Configuration",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Advanced thresholds used for decision support calculations.",
            fontSize = 11.sp,
            color = AquaticColors.SoftMutedText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Carrying Capacity
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stocking Capacity", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cycle.carryingCapacityRatio.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 6.0
                        onUpdate { c -> c.copy(carryingCapacityRatio = parsed) }
                    },
                    label = { Text("Carrying Capacity Ratio (kg/m²)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Standard intensive biofloc is ~6.0 kg/m².", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Disease Multiplier
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Disease Modeling", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cycle.diseaseMortalityMultiplier.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 2.5
                        onUpdate { c -> c.copy(diseaseMortalityMultiplier = parsed) }
                    },
                    label = { Text("Disease Mortality Multiplier (x)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Used to simulate the 'Disease Scenario' in the Harvest Optimizer.", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PL Gate Baselines
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("PL Quality Baselines", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cycle.week1SurvivalBaselineStock.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 85.0
                        onUpdate { c -> c.copy(week1SurvivalBaselineStock = parsed) }
                    },
                    label = { Text("STOCK Verdict Baseline (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cycle.week1SurvivalBaselineHold.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 75.0
                        onUpdate { c -> c.copy(week1SurvivalBaselineHold = parsed) }
                    },
                    label = { Text("HOLD Verdict Baseline (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cycle.week1SurvivalBaselineReject.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 50.0
                        onUpdate { c -> c.copy(week1SurvivalBaselineReject = parsed) }
                    },
                    label = { Text("REJECT Verdict Baseline (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
