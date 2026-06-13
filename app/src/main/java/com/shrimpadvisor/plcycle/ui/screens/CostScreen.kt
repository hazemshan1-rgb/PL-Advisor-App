package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors
import com.shrimpadvisor.plcycle.ui.CostRowItem
import com.shrimpadvisor.plcycle.ui.FcrGauge

/**
 * Screen 4: Cost & FCR Tracker
 */
@Composable
fun CostTrackerTab(
    cycle: PondCycle,
    result: AdvisorEngine.CostTrackingResult?,
    tempAdjustedFcr: Double? = null,
    onUpdate: ((PondCycle) -> PondCycle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("cost_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Feed & Operational Cost Logbook", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Tracks ongoing physical feed inputs, electricity, probiotics, and labor expenses.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.currentAbw.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 1.0
                            onUpdate { current -> current.copy(currentAbw = parsed) }
                        },
                        label = { Text("Average Weight (g)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_abw"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = cycle.totalFeedConsumed.let { if (it == 0.0) "" else it.toString() },
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(totalFeedConsumed = parsed) }
                        },
                        label = { Text("Total Feed (kg)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("input_pond_feed"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text("Fixed Cost Multipliers (USD)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cycle.feedCostPerKg.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onUpdate { current -> current.copy(feedCostPerKg = parsed) }
                    },
                    label = { Text("Feed Cost per kg ($)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.plUnitCost.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(plUnitCost = parsed) }
                        },
                        label = { Text("PL Unit cost ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cycle.aerationCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(aerationCostPerDay = parsed) }
                        },
                        label = { Text("Aeration/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cycle.probioticCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(probioticCostPerDay = parsed) }
                        },
                        label = { Text("Probiotic/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cycle.laborCostPerDay.toString(),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdate { current -> current.copy(laborCostPerDay = parsed) }
                        },
                        label = { Text("Labor/day ($)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Running Calculations & FCR Readout
        if (result != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dial gauge card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.width(160.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Feeding Efficiency", fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        FcrGauge(fcr = result.fcr)
                        // T1.5 — temperature-adjusted FCR
                        tempAdjustedFcr?.let { adj ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Temp-adj: ${String.format("%.2f", adj)}",
                                fontSize = 10.sp,
                                color = AquaticColors.SoftMutedText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Costs list card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Expense Breakdown", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        CostRowItem("Stock PL Cost", result.totalPlCost)
                        CostRowItem("Feed Expenses", result.totalFeedCost)
                        CostRowItem("Operational Costs", result.totalOperationalCost)

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Accumulated: ${String.format("$%,.2f", result.totalAccumulatedCost)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cost per kg: ${String.format("$%,.2f/kg", result.costPerKg)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
