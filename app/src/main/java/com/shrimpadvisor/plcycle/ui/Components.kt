package com.shrimpadvisor.plcycle.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.RegionProfile
import java.text.SimpleDateFormat
import java.util.*

// Theme helper colors
object AquaticColors {
    val DeepMarine = Color(0xFF031A2F)
    val deepOcean = Color(0xFF031A2F) // Alias for backwards compatibility
    val TealWater = Color(0xFF00796B)
    val ElectricTeal = Color(0xFF006874)
    val SandGold = Color(0xFFD84315)
    val SafeGreen = Color(0xFF2E7D32)
    val AlertOrange = Color(0xFFE65100)
    val AlarmRed = Color(0xFFC62828)
    val SoftMutedText = Color(0xFF5A6668)
    val GridLineColor = Color(0xFFDCE4E5)
}

@Composable
fun SummaryCard(
    title: String,
    statusText: String,
    statusColor: Color,
    scoreText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(22.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(3.dp))
                Text(statusText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = scoreText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun CostRowItem(label: String, valUSD: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
        Text(String.format("$%,.2f", valUSD), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FeedingReminderCard(pondName: String) {
    val context = LocalContext.current
    var remindersEnabled by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AquaticColors.ElectricTeal.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AquaticColors.ElectricTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Feeding Reminders", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (remindersEnabled) "Twice-daily reminders active (06:00 & 18:00)"
                    else "Enable to get morning & evening feeding alerts",
                    fontSize = 11.sp,
                    color = AquaticColors.SoftMutedText
                )
            }
            Switch(
                checked = remindersEnabled,
                onCheckedChange = { enabled ->
                    remindersEnabled = enabled
                    val workManager = androidx.work.WorkManager.getInstance(context)
                    if (enabled) {
                        val data = androidx.work.Data.Builder()
                            .putString(com.shrimpadvisor.plcycle.FeedingReminderWorker.KEY_POND_NAME, pondName)
                            .build()
                        val morningRequest = androidx.work.PeriodicWorkRequestBuilder<com.shrimpadvisor.plcycle.FeedingReminderWorker>(
                            12, java.util.concurrent.TimeUnit.HOURS
                        ).setInputData(data)
                            .addTag(com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG)
                            .build()
                        workManager.enqueueUniquePeriodicWork(
                            com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG,
                            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                            morningRequest
                        )
                    } else {
                        workManager.cancelAllWorkByTag(com.shrimpadvisor.plcycle.FeedingReminderWorker.WORK_TAG)
                    }
                }
            )
        }
    }
}

/**
 * 1. Animated FCR Gauge
 */
@Composable
fun FcrGauge(
    fcr: Double,
    modifier: Modifier = Modifier
) {
    val animatedFcr by animateFloatAsState(
        targetValue = fcr.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "FcrDial"
    )

    Column(
        modifier = modifier.testTag("fcr_gauge_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val labelColor = when {
            fcr < 1.1 -> AquaticColors.SandGold
            fcr in 1.1..1.5 -> AquaticColors.SafeGreen
            fcr in 1.51..1.8 -> AquaticColors.AlertOrange
            else -> AquaticColors.AlarmRed
        }

        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                drawArc(
                    color = AquaticColors.GridLineColor.copy(alpha = 0.5f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth - 4, cap = StrokeCap.Round)
                )

                val sweepMax = 270f
                val fcrClamped = animatedFcr.coerceIn(0.8f, 2.2f)
                val fraction = (fcrClamped - 0.8f) / (2.2f - 0.8f)
                val sweepAngle = fraction * sweepMax

                drawArc(
                    color = labelColor,
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.2f", fcr),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = labelColor
                )
                Text(
                    text = "FCR RATIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaticColors.SoftMutedText
                )
            }
        }
    }
}

/**
 * 2. Carrying Capacity Dual Progress Layout
 */
@Composable
fun BiomassCapacityBar(
    proposedBiomassKg: Double,
    maxSafeBiomassKg: Double,
    modifier: Modifier = Modifier
) {
    val ratio = proposedBiomassKg / maxSafeBiomassKg
    val progressColor = when {
        ratio < 0.75 -> AquaticColors.SafeGreen
        ratio in 0.75..1.0 -> AquaticColors.SandGold
        else -> AquaticColors.AlarmRed
    }

    val progressAnimated by animateFloatAsState(
        targetValue = ratio.toFloat().coerceIn(0.01f, 1.25f),
        animationSpec = tween(600),
        label = "biomassCapacity"
    )

    Column(modifier = modifier.fillMaxWidth().testTag("carrying_capacity_bar")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Biomass: ${String.format("%,.0f kg", proposedBiomassKg)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            Text(
                text = "Limit: ${String.format("%,.0f kg", maxSafeBiomassKg)} (80% Cap)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AquaticColors.SoftMutedText
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(AquaticColors.GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressAnimated.coerceAtMost(1.0f))
                    .background(progressColor, RoundedCornerShape(10.dp))
            )
            if (ratio > 1.0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = "EXCEEDED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * 3. Survival Curve Canvas Chart
 */
@Composable
fun SurvivalCurveChart(
    currentDay: Int,
    estimatedSurvival: Double,
    expectedSurvival: Double,
    historicalReadings: List<DailyReading> = emptyList(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("survival_curve_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gridCount = 5
            for (i in 0..gridCount) {
                val y = height * i / gridCount
                drawLine(
                    color = AquaticColors.GridLineColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }
            fun getCoordinates(day: Float, survivalPercent: Float): Offset {
                val x = if (30f == 1f) 0f else (day - 1) / (30f - 1) * width
                val scaledSurvival = survivalPercent.coerceIn(50f, 100f)
                val y = height - ((scaledSurvival - 50f) / (100f - 50f)) * height
                return Offset(x, y)
            }
            val expectedPath = Path().apply {
                val start = getCoordinates(1f, 100f)
                moveTo(start.x, start.y)
                val p7 = getCoordinates(7f, 92f)
                lineTo(p7.x, p7.y)
                val p30 = getCoordinates(30f, 75f)
                lineTo(p30.x, p30.y)
            }
            drawPath(
                path = expectedPath,
                color = AquaticColors.ElectricTeal.copy(alpha = 0.5f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            val visibleHistory = historicalReadings.filter { it.pondAge in 1..30 }
            if (visibleHistory.size >= 2) {
                val histPath = Path().apply {
                    val first = visibleHistory.first()
                    val pt = getCoordinates(first.pondAge.toFloat(), first.survivalPct.toFloat())
                    moveTo(pt.x, pt.y)
                    visibleHistory.drop(1).forEach { r ->
                        val p = getCoordinates(r.pondAge.toFloat(), r.survivalPct.toFloat())
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = histPath,
                    color = AquaticColors.SandGold.copy(alpha = 0.6f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            visibleHistory.forEach { r ->
                val pt = getCoordinates(r.pondAge.toFloat(), r.survivalPct.toFloat())
                drawCircle(color = AquaticColors.SandGold, radius = 4.dp.toPx(), center = pt)
            }
            if (currentDay in 1..30) {
                val currentPt = getCoordinates(currentDay.toFloat(), estimatedSurvival.toFloat())
                val dotColor = if (estimatedSurvival >= expectedSurvival - 5) AquaticColors.SafeGreen
                               else if (estimatedSurvival >= expectedSurvival - 15) AquaticColors.SandGold
                               else AquaticColors.AlarmRed
                drawCircle(color = dotColor, radius = 8.dp.toPx(), center = currentPt)
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = currentPt)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp, 3.dp).background(AquaticColors.ElectricTeal))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Standard Target Arc", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(AquaticColors.SafeGreen, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Your Measured Spot", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            }
        }
    }
}

/**
 * 4. Profitability/Net Gain Hold Simulator Bar Chart
 */
@Composable
fun ProfitScenarioBarChart(
    scenarios: List<com.shrimpadvisor.plcycle.ui.AdvisorEngine.HoldScenario>,
    bestHoldDay: Int?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(10.dp)
            .testTag("profit_scenarios_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val sampledIndices = listOf(0, 4, 9, 14, 19, 24, 29)
            val barCount = sampledIndices.size
            if (barCount == 0) return@Canvas
            val barWidth = (width / (barCount * 2))
            val maxGain = scenarios.maxOfOrNull { it.netAddedGain } ?: 100.0
            val minGain = scenarios.minOfOrNull { it.netAddedGain } ?: -100.0
            val absBound = maxOf(Math.abs(maxGain), Math.abs(minGain), 50.0)
            val zeroY = height / 2f + ((0f) / absBound.toFloat()) * (height / 2f)
            drawLine(color = AquaticColors.GridLineColor, start = Offset(0f, zeroY), end = Offset(width, zeroY), strokeWidth = 2f)
            sampledIndices.forEachIndexed { idx, scenarioIdx ->
                if (scenarioIdx < scenarios.size) {
                    val sc = scenarios[scenarioIdx]
                    val x = barWidth + idx * (barWidth * 2)
                    val valYVal = sc.netAddedGain.toFloat() / absBound.toFloat() * (height / 2f)
                    val barHeight = -valYVal
                    val isBest = sc.day == bestHoldDay
                    val barColor = when {
                        isBest -> AquaticColors.TealWater
                        sc.netAddedGain > 0 -> AquaticColors.ElectricTeal.copy(alpha = 0.7f)
                        else -> AquaticColors.AlarmRed.copy(alpha = 0.5f)
                    }
                    val rectTopY = if (barHeight < 0) zeroY + barHeight else zeroY
                    val rectHeight = Math.abs(barHeight)
                    drawRect(color = barColor, topLeft = Offset(x - barWidth / 2f, rectTopY), size = Size(barWidth, maxOf(2f, rectHeight)))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day 1", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            Text("Day 15", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
            Text("Day 30", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
        }
    }
}

/**
 * 5. Water Quality Trend Chart
 */
@Composable
fun WaterQualityTrendChart(
    readings: List<DailyReading>,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) return
    val sorted = readings.sortedBy { it.pondAge }
    val doMin = 0.0; val doMax = 10.0
    val phMin = 6.0; val phMax = 9.5
    val tanMin = 0.0; val tanMax = 1.0
    fun norm(value: Double, min: Double, max: Double): Float = ((value - min) / (max - min)).coerceIn(0.0, 1.0).toFloat()
    val days = sorted.map { it.pondAge.toFloat() }
    val minDay = days.first()
    val maxDay = days.last().coerceAtLeast(minDay + 1f)
    val metricLines = listOf(
        Pair(sorted.map { norm(it.doLevel, doMin, doMax) }, AquaticColors.ElectricTeal),
        Pair(sorted.map { norm(it.ph, phMin, phMax) }, AquaticColors.SandGold),
        Pair(sorted.map { norm(it.tanLevel, tanMin, tanMax) }, AquaticColors.AlarmRed)
    )
    Column(modifier = modifier) {
        Text(text = "Water Quality Trends", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)).padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                repeat(5) { i ->
                    val y = h * i / 4f
                    drawLine(AquaticColors.GridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }
                fun xPos(day: Float) = (day - minDay) / (maxDay - minDay) * w
                fun yPos(n: Float) = h - n * h
                metricLines.forEach { (values, color) ->
                    val path = Path().apply {
                        moveTo(xPos(days.first()), yPos(values.first()))
                        days.drop(1).forEachIndexed { idx, day -> lineTo(xPos(day), yPos(values[idx + 1])) }
                    }
                    drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    days.forEachIndexed { idx, day -> drawCircle(color, radius = 3.5.dp.toPx(), center = Offset(xPos(day), yPos(values[idx]))) }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(Triple("DO", AquaticColors.ElectricTeal, "mg/L"), Triple("pH", AquaticColors.SandGold, "6–9.5"), Triple("TAN", AquaticColors.AlarmRed, "mg/L")).forEach { (label, color, unit) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$label ($unit)", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectorCard(
    regionProfiles: List<RegionProfile>,
    activeRegionProfile: RegionProfile?,
    onSelectProfile: (RegionProfile?) -> Unit,
    onSaveProfile: (RegionProfile) -> Unit,
    onDeleteProfile: (RegionProfile) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showEditPanel by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<RegionProfile?>(null) }
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), modifier = Modifier.testTag("region_selector_card")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Regional Price Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Select a market region to apply price brackets.", fontSize = 11.sp, color = AquaticColors.SoftMutedText)
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = activeRegionProfile?.regionName ?: "Generic", onValueChange = {}, readOnly = true, label = { Text("Market Region", fontSize = 11.sp) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().menuAnchor().testTag("region_dropdown"))
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text("None", fontSize = 13.sp) }, onClick = { onSelectProfile(null); dropdownExpanded = false })
                    regionProfiles.forEach { profile ->
                        DropdownMenuItem(text = {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(profile.regionName, fontSize = 13.sp, fontWeight = if (profile.id == activeRegionProfile?.id) FontWeight.Bold else FontWeight.Normal)
                                    Text("${profile.currency} · ${profile.priceAt20g}-${profile.priceAt40g}", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                                }
                                if (!profile.isBuiltIn) { IconButton(onClick = { onDeleteProfile(profile); dropdownExpanded = false }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AquaticColors.AlarmRed, modifier = Modifier.size(16.dp)) } }
                            }
                        }, onClick = { onSelectProfile(profile); dropdownExpanded = false })
                    }
                }
            }
            if (activeRegionProfile != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row {
                        if (!activeRegionProfile.isBuiltIn) { IconButton(onClick = { editingProfile = activeRegionProfile; showEditPanel = true }, modifier = Modifier.size(28.dp)) { Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) } }
                        IconButton(onClick = { showEditPanel = !showEditPanel }, modifier = Modifier.size(28.dp)) { Icon(imageVector = if (showEditPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "Toggle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                    }
                }
                if (showEditPanel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RegionProfileDetailPanel(profile = activeRegionProfile, editable = !activeRegionProfile.isBuiltIn, onSave = onSaveProfile)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { editingProfile = RegionProfile(regionName = "", isBuiltIn = false); showEditPanel = true }, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Custom Region", fontSize = 12.sp)
            }
        }
    }
    if (editingProfile != null && showEditPanel) {
        RegionProfileEditDialog(profile = editingProfile!!, onDismiss = { editingProfile = null; showEditPanel = false }, onConfirm = { onSaveProfile(it); editingProfile = null; showEditPanel = false })
    }
}

@Composable
fun RegionProfileDetailPanel(profile: RegionProfile, editable: Boolean, onSave: (RegionProfile) -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Price per kg (${profile.currency})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("20g" to profile.priceAt20g, "25g" to profile.priceAt25g, "30g" to profile.priceAt30g, "35g" to profile.priceAt35g, "40g" to profile.priceAt40g).forEach { (label, price) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                        Text(String.format("%.1f", price), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun RegionProfileEditDialog(profile: RegionProfile, onDismiss: () -> Unit, onConfirm: (RegionProfile) -> Unit) {
    var name by remember { mutableStateOf(profile.regionName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Edit Region") }, text = {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
    }, confirmButton = { Button(onClick = { onConfirm(profile.copy(regionName = name)); onDismiss() }) { Text("Save") } })
}

@Composable
fun HarvestCalendarStrip(stockingDate: Long, currentAge: Int, optimalHoldDay: Int?, shouldHarvestNow: Boolean) {
    val startDoc = (currentAge - 5).coerceAtLeast(0)
    val count = 40
    val optimalDoc = if (optimalHoldDay != null) currentAge + optimalHoldDay else null
    val listState = rememberLazyListState()
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        items(count) { index ->
            val doc = startDoc + index
            val isCurrent = doc == currentAge
            val isOptimal = doc == optimalDoc
            val bgColor = when { isCurrent -> MaterialTheme.colorScheme.primary; isOptimal -> AquaticColors.SafeGreen; else -> MaterialTheme.colorScheme.surface }
            Column(modifier = Modifier.width(48.dp).background(bgColor, RoundedCornerShape(8.dp)).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("D$doc", fontSize = 10.sp, color = if (isCurrent || isOptimal) Color.White else Color.Unspecified)
                Text(dateFormat.format(Date(stockingDate + doc * 86400000L)), fontSize = 8.sp, color = if (isCurrent || isOptimal) Color.White else Color.Unspecified)
            }
        }
    }
}
