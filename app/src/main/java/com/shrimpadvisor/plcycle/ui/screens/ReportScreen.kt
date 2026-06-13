package com.shrimpadvisor.plcycle.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.shrimpadvisor.plcycle.ui.AdvisorEngine
import com.shrimpadvisor.plcycle.ui.AquaticColors
import com.shrimpadvisor.plcycle.ui.XlsxExporter

/**
 * Screen 6: Complete Cycle Report
 */
@Composable
fun ReportSummaryTab(
    cycle: PondCycle,
    plResult: AdvisorEngine.QualityResult?,
    stocking: AdvisorEngine.StockingResult?,
    survival: AdvisorEngine.SurvivalTrajectoryResult?,
    cost: AdvisorEngine.CostTrackingResult?,
    harvest: AdvisorEngine.HarvestOptimizerResult?,
    dailyReadings: List<DailyReading> = emptyList()
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showCopiedToast by remember { mutableStateOf(false) }

    val reportText = """
=== SHRIMP PL CYCLE ADVISOR REPORT ===
Pond Name: ${cycle.pondName}
Evaluation Date: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(cycle.stockingDate))}
Pond Specs: ${cycle.pondSize} m² | Current Age: ${cycle.currentAge} Days

[1] PL PRE-STOCKING QUALITY
Verdict: ${plResult?.statusMessage ?: "--"}
Avg Quality rating: ${plResult?.score?.let { String.format("%.1f%%", it) } ?: "--"}

[2] STOCKING & CAPACITIES
Proposed density: ${cycle.proposedDensity} PL/m²
Total optimal recommended quantity: ${stocking?.totalOptimalQty?.toInt() ?: "--"} PL
Safe carrying limit capacity exceeded: ${stocking?.carryingCapacityExceeded ?: false}

[3] SURVIVAL TRAJECTORY
Current estimated survival: ${cycle.estimatedSurvival}%
Expected survival trend: ${survival?.expectedSurvival?.let { String.format("%.1f%%", it) } ?: "--"}
Diagnostics isolates: ${survival?.classification ?: "--"}

[4] MONEY TRACKING
FCR Index: ${cost?.fcr?.let { String.format("%.2f", it) } ?: "--"}
Accumulated Expenses: ${cost?.totalAccumulatedCost?.let { String.format("$%,.2f", it) } ?: "--"}
Cost per kg: ${cost?.costPerKg?.let { String.format("$%,.2f/kg", it) } ?: "--"}

[5] HARVEST OPTIMIZATION
Best Action: ${if (harvest?.shouldHarvestNow == true) "HARVEST NOW" else "HOLD GROWING"}
Best holding scenario peak: ${harvest?.bestHoldScenario?.let { "Hold ${it.day} more Days" } ?: "Harvest right now"}
Projected profitability uplift: ${harvest?.profitDifferential?.let { String.format("$%,.2f", it) } ?: "--"}
======================================
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("report_tab")
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 Comprehensive Cycle Report",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Consolidated summaries across all 5 operational vectors. Share or export copy to files or team chat.",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030D1A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color(0xFFA1B3C6),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(reportText))
                            showCopiedToast = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_report_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                putExtra(Intent.EXTRA_SUBJECT, "Shrimp Report — ${cycle.pondName}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_report_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }

                if (dailyReadings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val pdfDoc = android.graphics.pdf.PdfDocument()
                            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                            val page = pdfDoc.startPage(pageInfo)
                            val canvas = page.canvas
                            val paint = android.graphics.Paint().apply { isAntiAlias = true }
                            var y = 60f

                            fun drawLine(text: String, size: Float = 12f, bold: Boolean = false) {
                                paint.textSize = size
                                paint.typeface = if (bold) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                                canvas.drawText(text, 40f, y, paint)
                                y += size + 6f
                            }

                            drawLine("SHRIMP PL CYCLE ADVISOR — FULL REPORT", 16f, true)
                            drawLine("Pond: ${cycle.pondName}  |  Area: ${cycle.pondSize} m²  |  Age: ${cycle.currentAge} days", 11f)
                            drawLine("Generated: ${java.text.DateFormat.getDateInstance().format(java.util.Date())}", 10f)
                            y += 8f
                            drawLine("─────────────────────────────────────────────────", 10f)
                            y += 4f
                            drawLine("PL Quality: ${plResult?.statusMessage ?: "N/A"}  (${plResult?.score?.let { String.format("%.1f%%", it) } ?: "--"})", 11f)
                            drawLine("Stocking Density: ${cycle.proposedDensity} PL/m²  |  Capacity exceeded: ${stocking?.carryingCapacityExceeded ?: false}", 11f)
                            drawLine("Survival: ${cycle.estimatedSurvival}%  |  Expected: ${survival?.expectedSurvival?.let { String.format("%.1f%%", it) } ?: "--"}", 11f)
                            drawLine("FCR: ${cost?.fcr?.let { String.format("%.2f", it) } ?: "--"}  |  Total cost: ${cost?.totalAccumulatedCost?.let { String.format("$%,.2f", it) } ?: "--"}", 11f)
                            drawLine("Harvest: ${if (harvest?.shouldHarvestNow == true) "HARVEST NOW" else "HOLD"}  |  Uplift: ${harvest?.profitDifferential?.let { String.format("$%,.2f", it) } ?: "--"}", 11f)

                            if (dailyReadings.isNotEmpty()) {
                                y += 8f
                                drawLine("─────────────────────────────────────────────────", 10f)
                                drawLine("DAILY READINGS", 12f, true)
                                drawLine("Day  Survival%  DO    TAN   pH    Temp  ABW", 9f, true)
                                dailyReadings.sortedBy { it.pondAge }.forEach { r ->
                                    if (y < 800f) {
                                        drawLine(
                                            String.format(
                                                "%-4d %-9.1f  %-5.2f %-5.2f %-5.1f %-5.1f %-5.1f",
                                                r.pondAge, r.survivalPct, r.doLevel, r.tanLevel, r.ph, r.temp, r.abw
                                            ), 9f
                                        )
                                    }
                                }
                            }

                            pdfDoc.finishPage(page)
                            val pdfFileName = "${cycle.pondName.replace(" ", "_")}_report.pdf"
                            val pdfFile = java.io.File(context.cacheDir, pdfFileName)
                            pdfFile.outputStream().use { pdfDoc.writeTo(it) }
                            pdfDoc.close()
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Shrimp Report — ${cycle.pondName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export PDF"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_pdf_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Full Report (PDF)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val csv = buildString {
                                appendLine("Day,Survival %,DO (mg/L),TAN (mg/L),pH,Temp (°C),ABW (g),Notes")
                                dailyReadings.sortedBy { it.pondAge }.forEach { r ->
                                    val notes = r.notes.replace(",", ";").replace("\n", " ")
                                    appendLine("${r.pondAge},${r.survivalPct},${r.doLevel},${r.tanLevel},${r.ph},${r.temp},${r.abw},$notes")
                                }
                            }
                            val fileName = "${cycle.pondName.replace(" ", "_")}_readings.csv"
                            val file = java.io.File(context.cacheDir, fileName)
                            file.writeText(csv)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Daily Readings — ${cycle.pondName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export CSV"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Daily Readings (CSV)", fontWeight = FontWeight.Bold)
                    }

                    // T3.4 — XLSX export
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val xlsxFile = XlsxExporter.export(context, cycle, dailyReadings)
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", xlsxFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Daily Readings — ${cycle.pondName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export XLSX"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_xlsx_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export XLSX", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Daily Readings (XLSX)", fontWeight = FontWeight.Bold)
                    }
                }

                if (showCopiedToast) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AquaticColors.SafeGreen.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Copied report successfully!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticColors.SafeGreen,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
