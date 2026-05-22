package com.shrimpadvisor.plcycle.ui

import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// --- Gemini REST API models ---
data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>, val role: String = "user")
data class GeminiRequest(val contents: List<GeminiContent>)
data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)
data class GeminiCandidate(val content: GeminiContent? = null)

// --- In-session chat message ---
data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean
)

object GeminiAdvisor {

    private const val BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    internal fun buildPrompt(
        cycle: PondCycle,
        question: String,
        recentReadings: List<DailyReading> = emptyList()
    ) = buildString {
        appendLine("You are an expert shrimp aquaculture advisor specializing in intensive L. vannamei (whiteleg shrimp) biofloc farming.")
        appendLine("Provide concise, actionable advice — 3 to 5 sentences unless a detailed protocol is explicitly requested.")
        appendLine()
        appendLine("=== Active Pond Data: ${cycle.pondName} ===")
        appendLine("Cycle age: ${cycle.currentAge} days")
        appendLine("Pond size: ${cycle.pondSize} m²  |  Stocking density: ${cycle.proposedDensity} PL/m²")
        appendLine("Water quality: DO ${cycle.doLevel} ppm | pH ${cycle.ph} | Salinity ${cycle.salinity} ppt | Temp ${cycle.temp}°C | TAN ${cycle.tanLevel} ppm")
        appendLine("Survival: ${cycle.estimatedSurvival}%  |  ABW: ${cycle.currentAbw} g  |  ADG: ${cycle.averageDailyGain} g/day")
        appendLine("Feed consumed: ${cycle.totalFeedConsumed} kg  |  Feed cost: ${cycle.feedCostPerKg} USD/kg")
        appendLine("PL quality scores — stress: ${cycle.stressToleranceScore}%  gut: ${cycle.gutFullnessScore}%  supplier: ${cycle.supplierScore}%")

        if (recentReadings.isNotEmpty()) {
            val sorted = recentReadings.sortedBy { it.pondAge }
            val totalDays = sorted.last().pondAge

            // T2.5: compress old data into weekly summaries to stay within token budget
            if (totalDays > 14) {
                val cutoff = totalDays - 14
                val archiveReadings = sorted.filter { it.pondAge <= cutoff }
                val recentWindow = sorted.filter { it.pondAge > cutoff }

                // Group archive by week and emit one summary line per week
                val weekGroups = archiveReadings.groupBy { (it.pondAge - 1) / 7 + 1 }
                appendLine()
                appendLine("=== Historical Weekly Summaries (DOC 1–${cutoff}) ===")
                for ((week, wReadings) in weekGroups.entries.sortedBy { it.key }) {
                    fun avg(values: List<Double>) = if (values.isEmpty()) 0.0 else values.sum() / values.size
                    appendLine(
                        String.format(
                            "Week %d: DO=%.1f  TAN=%.2f  pH=%.2f  Temp=%.1f  ABW=%.1fg  Surv=%.1f%%",
                            week,
                            avg(wReadings.map { it.doLevel }),
                            avg(wReadings.map { it.tanLevel }),
                            avg(wReadings.map { it.ph }),
                            avg(wReadings.map { it.temp }),
                            avg(wReadings.map { it.abw }),
                            avg(wReadings.map { it.survivalPct })
                        )
                    )
                }

                appendLine()
                appendLine("=== Last 14 Days Detail (DOC ${cutoff + 1}–${totalDays}) ===")
                appendLine("Day | DO  | TAN  | pH   | Temp | ABW  | Surv%")
                appendLine("----|-----|------|------|------|------|------")
                for (r in recentWindow) {
                    appendLine(
                        String.format(
                            "%3d | %3.1f | %4.2f | %4.2f | %4.1f | %4.1f | %5.1f",
                            r.pondAge, r.doLevel, r.tanLevel, r.ph, r.temp, r.abw, r.survivalPct
                        )
                    )
                }
            } else {
                val window = sorted.takeLast(14)
                appendLine()
                appendLine("=== Last ${window.size} Daily Readings ===")
                appendLine("Day | DO  | TAN  | pH   | Temp | ABW  | Surv%")
                appendLine("----|-----|------|------|------|------|------")
                for (r in window) {
                    appendLine(
                        String.format(
                            "%3d | %3.1f | %4.2f | %4.2f | %4.1f | %4.1f | %5.1f",
                            r.pondAge, r.doLevel, r.tanLevel, r.ph, r.temp, r.abw, r.survivalPct
                        )
                    )
                }
            }

            // 7-day trend analysis: compare first-half avg vs second-half avg
            appendLine()
            appendLine("=== 7-Day Trends ===")

            val trendReadings = sorted.takeLast(14)
            if (trendReadings.size >= 2) {
                val mid = trendReadings.size / 2
                val firstHalf = trendReadings.take(mid)
                val secondHalf = trendReadings.drop(mid)

                fun trendLabel(firstAvg: Double, secondAvg: Double, range: Double): String {
                    val delta = secondAvg - firstAvg
                    val threshold = range * 0.05
                    return when {
                        delta > threshold -> "rising"
                        delta < -threshold -> "falling"
                        else -> "stable"
                    }
                }

                fun avg(values: List<Double>) = if (values.isEmpty()) 0.0 else values.sum() / values.size

                // DO
                val doFirst = avg(firstHalf.map { it.doLevel })
                val doSecond = avg(secondHalf.map { it.doLevel })
                val doAll = trendReadings.map { it.doLevel }
                val doRange = (doAll.maxOrNull() ?: 0.0) - (doAll.minOrNull() ?: 0.0)
                val doAvg = avg(doAll)
                val doLast = trendReadings.last().doLevel
                val doDelta = doLast - doFirst
                appendLine(
                    String.format(
                        "DO: %s (avg %.1f, last %.1f, Δ %+.1f)",
                        trendLabel(doFirst, doSecond, doRange), doAvg, doLast, doDelta
                    )
                )

                // TAN
                val tanFirst = avg(firstHalf.map { it.tanLevel })
                val tanSecond = avg(secondHalf.map { it.tanLevel })
                val tanAll = trendReadings.map { it.tanLevel }
                val tanRange = (tanAll.maxOrNull() ?: 0.0) - (tanAll.minOrNull() ?: 0.0)
                val tanAvg = avg(tanAll)
                val tanLast = trendReadings.last().tanLevel
                val tanDelta = tanLast - tanFirst
                appendLine(
                    String.format(
                        "TAN: %s (avg %.2f, last %.2f, Δ %+.2f)",
                        trendLabel(tanFirst, tanSecond, tanRange), tanAvg, tanLast, tanDelta
                    )
                )

                // pH
                val phFirst = avg(firstHalf.map { it.ph })
                val phSecond = avg(secondHalf.map { it.ph })
                val phAll = trendReadings.map { it.ph }
                val phRange = (phAll.maxOrNull() ?: 0.0) - (phAll.minOrNull() ?: 0.0)
                val phAvg = avg(phAll)
                val phLast = trendReadings.last().ph
                val phDelta = phLast - phFirst
                appendLine(
                    String.format(
                        "pH: %s (avg %.2f, last %.2f, Δ %+.2f)",
                        trendLabel(phFirst, phSecond, phRange), phAvg, phLast, phDelta
                    )
                )

                // Survival
                val survFirst = avg(firstHalf.map { it.survivalPct })
                val survSecond = avg(secondHalf.map { it.survivalPct })
                val survAll = trendReadings.map { it.survivalPct }
                val survRange = (survAll.maxOrNull() ?: 0.0) - (survAll.minOrNull() ?: 0.0)
                val survAvg = avg(survAll)
                val survLast = trendReadings.last().survivalPct
                val survDelta = survLast - survFirst
                appendLine(
                    String.format(
                        "Survival: %s (avg %.1f%%, last %.1f%%, Δ %+.1f%%)",
                        trendLabel(survFirst, survSecond, survRange), survAvg, survLast, survDelta
                    )
                )

                // ABW
                val abwFirst = avg(firstHalf.map { it.abw })
                val abwSecond = avg(secondHalf.map { it.abw })
                val abwAll = trendReadings.map { it.abw }
                val abwRange = (abwAll.maxOrNull() ?: 0.0) - (abwAll.minOrNull() ?: 0.0)
                val abwAvg = avg(abwAll)
                val abwLast = trendReadings.last().abw
                val abwDelta = abwLast - abwFirst
                appendLine(
                    String.format(
                        "ABW: %s (avg %.1fg, last %.1fg, Δ %+.1fg)",
                        trendLabel(abwFirst, abwSecond, abwRange), abwAvg, abwLast, abwDelta
                    )
                )
            }
        }

        appendLine()
        appendLine("Farmer's question: $question")
    }

    suspend fun ask(
        apiKey: String,
        cycle: PondCycle,
        question: String,
        recentReadings: List<DailyReading> = emptyList()
    ): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "⚠️ No API key configured. Add your GEMINI_API_KEY to the .env file and rebuild the app."
            }

            try {
                val body = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(buildPrompt(cycle, question, recentReadings))))
                    )
                )
                val json = requestAdapter.toJson(body)

                val httpRequest = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(httpRequest).execute()
                if (!response.isSuccessful) {
                    return@withContext "API error ${response.code}: ${response.body?.string()?.take(200)}"
                }

                val responseJson = response.body?.string()
                    ?: return@withContext "Empty response from AI service."

                responseAdapter.fromJson(responseJson)
                    ?.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                    ?: "No response generated. Please try again."
            } catch (e: java.io.IOException) {
                // T3.5: offline fallback — network unreachable
                AdvisorEngine.generateOfflineAdvice(cycle)
            } catch (e: Exception) {
                "Connection error: ${e.message ?: "Unable to reach AI advisor."}"
            }
        }
}
