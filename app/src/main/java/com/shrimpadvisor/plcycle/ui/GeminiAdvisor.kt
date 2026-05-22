package com.shrimpadvisor.plcycle.ui

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

    internal fun buildPrompt(cycle: PondCycle, question: String) = buildString {
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
        appendLine()
        appendLine("Farmer's question: $question")
    }

    suspend fun ask(apiKey: String, cycle: PondCycle, question: String): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "⚠️ No API key configured. Add your GEMINI_API_KEY to the .env file and rebuild the app."
            }

            try {
                val body = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(buildPrompt(cycle, question))))
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
            } catch (e: Exception) {
                "Connection error: ${e.message ?: "Unable to reach AI advisor."}"
            }
        }
}
