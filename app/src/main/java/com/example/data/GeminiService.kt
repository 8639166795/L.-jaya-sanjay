package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getAiInsights(
        city: String,
        temp: Double,
        apparentTemp: Double,
        humidity: Double,
        precipitationProbability: Double,
        wind: Double,
        uvMax: Double,
        weatherDesc: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Get customized AI suggestions on clothing, skin safety, and leisure activities by saving your Gemini API Key in the AI Studio Secrets panel."
        }

        val prompt = """
            Summarize the weather in $city and give travel/apparel advice:
            - Temperature: ${temp}°C (Feels like ${apparentTemp}°C)
            - Conditions: $weatherDesc
            - Precipitation Chance: ${precipitationProbability}%
            - Relative Humidity: ${humidity}%
            - Winds: ${wind} km/h
            - Max UV index: $uvMax

            Format into 3 extremely short visual bullet points:
            • 🧥 *Wear*: (Best outfit choice for this weather)
            • 🧭 *Plan*: (Indoor vs outdoor activity guide)
            • 💡 *Tip*: (Sun UV alert, umbrella alert, wind chill, hydration alert, etc.)

            Make it engaging and direct. Total length must be under 45 words.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Insights generated are empty. Try refreshing."
        } catch (e: Exception) {
            "AI insights temporarily unavailable. Add your Gemini API Key to enable recommendations."
        }
    }
}
