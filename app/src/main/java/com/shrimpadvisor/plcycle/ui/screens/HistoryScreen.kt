package com.shrimpadvisor.plcycle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.ui.AquaticColors

/**
 * Screen 8: Reading History
 */
@Composable
fun ReadingHistoryTab(
    readings: List<DailyReading>,
    onDelete: (DailyReading) -> Unit
) {
    val sorted = remember(readings) { readings.sortedByDescending { it.timestamp } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            "Daily Reading Log",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "${sorted.size} readings recorded",
            fontSize = 11.sp,
            color = AquaticColors.SoftMutedText
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (sorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No readings logged yet.\nUse the Survival tab to log your first reading.",
                    textAlign = TextAlign.Center, color = AquaticColors.SoftMutedText, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sorted, key = { it.id }) { reading ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("DOC", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                                Text("${reading.pondAge}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ReadingChip("DO", String.format("%.1f", reading.doLevel),
                                        if (reading.doLevel < 5.3) AquaticColors.AlarmRed else AquaticColors.SafeGreen)
                                    ReadingChip("TAN", String.format("%.2f", reading.tanLevel),
                                        if (reading.tanLevel >= 0.8) AquaticColors.AlarmRed else AquaticColors.SafeGreen)
                                    ReadingChip("pH", String.format("%.1f", reading.ph),
                                        if (reading.ph < 7.3 || reading.ph > 8.7) AquaticColors.AlarmRed else AquaticColors.SafeGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ReadingChip("ABW", "${String.format("%.1f", reading.abw)}g",
                                        AquaticColors.ElectricTeal)
                                    ReadingChip("Surv", "${String.format("%.0f", reading.survivalPct)}%",
                                        AquaticColors.TealWater)
                                    if (reading.feedGiven > 0) {
                                        ReadingChip("Feed", "${String.format("%.1f", reading.feedGiven)}kg",
                                            AquaticColors.SandGold)
                                    }
                                }
                            }

                            IconButton(onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete",
                                    tint = AquaticColors.AlarmRed.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete Reading?") },
                            text = { Text("Remove DOC ${reading.pondAge} reading. This cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = { onDelete(reading); showDeleteConfirm = false }) {
                                    Text("Delete", color = AquaticColors.AlarmRed)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 10.sp, color = color)
    }
}
