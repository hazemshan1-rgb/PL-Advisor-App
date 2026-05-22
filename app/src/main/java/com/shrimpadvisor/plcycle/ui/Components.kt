package com.shrimpadvisor.plcycle.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shrimpadvisor.plcycle.data.DailyReading
import kotlin.math.cos
import kotlin.math.sin

// Theme helper colors
object AquaticColors {
    val DeepMarine = Color(0xFF031A2F)
    val TealWater = Color(0xFF00796B) // polished emerald-teal green
    val ElectricTeal = Color(0xFF006874) // Professional cyan teal
    val SandGold = Color(0xFFD84315) // Amber gold / deep terracotta
    val SafeGreen = Color(0xFF2E7D32) // Forest moss safe green
    val AlertOrange = Color(0xFFE65100) // Warning warning orange
    val AlarmRed = Color(0xFFC62828) // Critical warning red
    val SoftMutedText = Color(0xFF5A6668) // Sophisticated blue-grey text
    val GridLineColor = Color(0xFFDCE4E5) // Clean border line
}

/**
 * 1. Animated FCR Gauge
 * Renders a sweep gauge indicating FCR efficiency (1.0 to 2.2+)
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

                // Background track
                drawArc(
                    color = AquaticColors.GridLineColor.copy(alpha = 0.5f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth - 4, cap = StrokeCap.Round)
                )

                // Colored active track helper
                val sweepMax = 270f
                // Map fcr in ranges 0.8 to 2.2 onto 0f to 270f
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
 * Compare proposed biomass loading vs the absolute floor Carrying Capacity.
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
            // Draw progress bar based on fraction
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressAnimated.coerceAtMost(1.0f))
                    .background(progressColor, RoundedCornerShape(10.dp))
            )
            // If overflow exceeds carrying capacity limits
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
 * Plots expected decay curve vs the actual measured point over days 1-30.
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

            // 1. Draw Grid Lines & Labels
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

            // 2. Plot lines
            // Point calculations:
            // x from day 1 to day 30 maps to 0f to width
            // y from 50% to 100% survival maps to height down to 0f
            fun getCoordinates(day: Float, survivalPercent: Float): Offset {
                // map day [1..30] to [0..width]
                val x = (day - 1) / (30f - 1) * width
                // map survival [50..100] to [height..0]
                val scaledSurvival = survivalPercent.coerceIn(50f, 100f)
                val y = height - ((scaledSurvival - 50f) / (100f - 50f)) * height
                return Offset(x, y)
            }

            // Expected path (linear 100% to 75% at day 30)
            val expectedPath = Path().apply {
                val start = getCoordinates(1f, 100f)
                moveTo(start.x, start.y)
                // Day 7 is 92%, Day 30 is 75%
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

            // Historical reading dots — connect with a thin line then mark each point
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

            // Current user sampling point
            if (currentDay in 1..30) {
                val currentPt = getCoordinates(currentDay.toFloat(), estimatedSurvival.toFloat())

                // Draw a marker circle
                val dotColor = if (estimatedSurvival >= expectedSurvival - 5) {
                    AquaticColors.SafeGreen
                } else if (estimatedSurvival >= expectedSurvival - 15) {
                    AquaticColors.SandGold
                } else {
                    AquaticColors.AlarmRed
                }

                drawCircle(
                    color = dotColor,
                    radius = 8.dp.toPx(),
                    center = currentPt
                )

                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = currentPt
                )
            }
        }

        // Legends
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
 * Shows potential gains or losses for holding shrimp N days (scenarios scan from 1 to 30)
 */
@Composable
fun ProfitScenarioBarChart(
    scenarios: List<AdvisorEngine.HoldScenario>,
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

            // We only show a sample of scenarios to fit nicely: days 1, 5, 10, 15, 20, 25, 30
            val sampledIndices = listOf(0, 4, 9, 14, 19, 24, 29)
            val barCount = sampledIndices.size
            val barWidth = (width / (barCount * 2))

            // Find global min and max of gains to scale drawing nicely
            val maxGain = scenarios.maxOfOrNull { it.netAddedGain } ?: 100.0
            val minGain = scenarios.minOfOrNull { it.netAddedGain } ?: -100.0
            val absBound = maxOf(Math.abs(maxGain), Math.abs(minGain), 50.0)

            // Draw baseline (Zero profit axis)
            // Map 0 in [ -absBound .. absBound ] onto height
            val zeroY = height / 2f + ((0f) / absBound.toFloat()) * (height / 2f)

            drawLine(
                color = AquaticColors.GridLineColor,
                start = Offset(0f, zeroY),
                end = Offset(width, zeroY),
                strokeWidth = 2f
            )

            // Plot sample bars
            sampledIndices.forEachIndexed { idx, scenarioIdx ->
                if (scenarioIdx < scenarios.size) {
                    val sc = scenarios[scenarioIdx]
                    val x = barWidth + idx * (barWidth * 2)

                    // Calculate bar heights
                    // val fraction = sc.netAddedGain / absBound
                    // y relative to zero axis
                    val valYVal = sc.netAddedGain.toFloat() / absBound.toFloat() * (height / 2f)
                    val barHeight = -valYVal // negative because negative y goes UP in screens

                    val isBest = sc.day == bestHoldDay
                    val barColor = when {
                        isBest -> AquaticColors.TealWater
                        sc.netAddedGain > 0 -> AquaticColors.ElectricTeal.copy(alpha = 0.7f)
                        else -> AquaticColors.AlarmRed.copy(alpha = 0.5f)
                    }

                    // Top Left and Size
                    val rectTopY = if (barHeight < 0) zeroY + barHeight else zeroY
                    val rectHeight = Math.abs(barHeight)

                    drawRect(
                        color = barColor,
                        topLeft = Offset(x - barWidth / 2f, rectTopY),
                        size = Size(barWidth, maxOf(2f, rectHeight))
                    )
                }
            }
        }

        // Axes descriptions
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
 * 4. Water Quality Trend Chart
 * Plots DO, pH, and TAN across logged daily readings.
 * Each metric is normalised to its own safe operating range (0–1) so all three
 * lines fit the same canvas height without distortion.
 *
 * Colour coding:
 *   DO  → Electric Teal
 *   pH  → Sand Gold
 *   TAN → Alarm Red
 */
@Composable
fun WaterQualityTrendChart(
    readings: List<DailyReading>,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) return

    val sorted = readings.sortedBy { it.pondAge }

    // Safe-range normalisation bounds for each metric.
    // Values outside range clamp to 0 or 1 — intentional; they'll visually breach the safe zone.
    val doMin = 0.0; val doMax = 10.0
    val phMin = 6.0; val phMax = 9.5
    val tanMin = 0.0; val tanMax = 1.0

    fun norm(value: Double, min: Double, max: Double): Float =
        ((value - min) / (max - min)).coerceIn(0.0, 1.0).toFloat()

    val days = sorted.map { it.pondAge.toFloat() }
    val minDay = days.first()
    val maxDay = days.last().coerceAtLeast(minDay + 1f)

    // Pre-compute normalised value lists outside the draw scope (data class not allowed inside DrawScope lambda)
    val doValues = sorted.map { norm(it.doLevel, doMin, doMax) }
    val phValues = sorted.map { norm(it.ph, phMin, phMax) }
    val tanValues = sorted.map { norm(it.tanLevel, tanMin, tanMax) }
    val metricLines = listOf(
        Pair(doValues, AquaticColors.ElectricTeal),
        Pair(phValues, AquaticColors.SandGold),
        Pair(tanValues, AquaticColors.AlarmRed)
    )

    Column(modifier = modifier) {
        Text(
            text = "Water Quality Trends",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Horizontal grid lines
                repeat(5) { i ->
                    val y = h * i / 4f
                    drawLine(AquaticColors.GridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                }

                fun xPos(day: Float) = (day - minDay) / (maxDay - minDay) * w
                fun yPos(n: Float) = h - n * h

                metricLines.forEach { (values, color) ->
                    val path = Path().apply {
                        moveTo(xPos(days.first()), yPos(values.first()))
                        days.drop(1).forEachIndexed { idx, day ->
                            lineTo(xPos(day), yPos(values[idx + 1]))
                        }
                    }
                    drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    days.forEachIndexed { idx, day ->
                        drawCircle(color, radius = 3.5.dp.toPx(), center = Offset(xPos(day), yPos(values[idx])))
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(
                Triple("DO", AquaticColors.ElectricTeal, "mg/L"),
                Triple("pH", AquaticColors.SandGold, "6–9.5"),
                Triple("TAN", AquaticColors.AlarmRed, "mg/L")
            ).forEach { (label, color, unit) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$label ($unit)", fontSize = 10.sp, color = AquaticColors.SoftMutedText)
                }
            }
        }
    }
}
