package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.shrimpadvisor.plcycle.ui.BiomassCapacityBar

/**
 * Screen 2: Stocking Decision Engine
 */
@Composable
fun StockingEngineTab(
    cycle: PondCycle,
    result: AdvisorEngine.StockingResult?,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("stocking_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stocking Densities & Water Indices", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Enforces water security envelopes and computes safe stocking bio-loads.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                // Physical metrics fields
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.pondSize.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(pondSize = parsed) }
                        },
                        label = { Text("Pond Size (m²)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_size"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.proposedDensity.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(proposedDensity = parsed) }
                        },
                        label = { Text("Density (PL/m²)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_density"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cycle.harvestWeightTarget.let { if (it == 0.0) "" else it.toString() },
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onUpdate { current -> current.copy(harvestWeightTarget = parsed) }
                    },
                    label = { Text("Target Harvest Body Weight (grams)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_pond_weight_target"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Active Water Metrics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                // Oxygen Slider
                WaterMetricSlider(
                    label = "Dissolved Oxygen (DO)",
                    value = cycle.doLevel,
                    range = 1.0f..10.0f,
                    unit = " ppm",
                    onValueChange = { onUpdate { c -> c.copy(doLevel = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // pH Slider
                WaterMetricSlider(
                    label = "pH Index",
                    value = cycle.ph,
                    range = 5.0f..10.0f,
                    unit = "",
                    onValueChange = { onUpdate { c -> c.copy(ph = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Salinity Slider
                WaterMetricSlider(
                    label = "Salinity",
                    value = cycle.salinity,
                    range = 5.0f..45.0f,
                    unit = " ppt",
                    onValueChange = { onUpdate { c -> c.copy(salinity = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature Slider
                WaterMetricSlider(
                    label = "Temperature",
                    value = cycle.temp,
                    range = 20.0f..38.0f,
                    unit = " °C",
                    onValueChange = { onUpdate { c -> c.copy(temp = it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // TAN Level
                WaterMetricSlider(
                    label = "Ammonia (TAN)",
                    value = cycle.tanLevel,
                    range = 0.0f..2.5f,
                    unit = " ppm",
                    onValueChange = { onUpdate { c -> c.copy(tanLevel = it) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculations & Results Visuals
        if (result != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bio-Load Carrying Capacity Check", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    BiomassCapacityBar(
                        proposedBiomassKg = result.proposedBiomass,
                        maxSafeBiomassKg = result.maxSafeBiomass
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Optimal Density recommendation: ${result.optimalDensity.toInt()} PL/m² (total: ${String.format("%,.0f", result.totalOptimalQty)} PL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Water Quality Stability Checklist:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    result.parameterChecks.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (check.isOptimal) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (check.isOptimal) AquaticColors.SafeGreen else AquaticColors.AlarmRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(check.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(check.valueString, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                Text(check.rangeMessage, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterMetricSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = String.format("%.1f", value) + unit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
            modifier = Modifier.height(28.dp)
        )
    }
}
