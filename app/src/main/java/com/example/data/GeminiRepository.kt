package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null,
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null // "user" or "model"
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val thinkingConfig: ThinkingConfig? = null
)

@Serializable
data class ThinkingConfig(
    val thinkingLevel: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST
    @Streaming
    suspend fun generateContentStream(
        @Url url: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    // Conversation history
    private val chatHistory = mutableListOf<Content>()

    suspend fun sendChatMessage(
        message: String,
        modelName: String = "gemini-3.1-pro-preview",
        useHighThinking: Boolean = true
    ): Flow<String> = flow {
        // Add user message to history
        chatHistory.add(Content(parts = listOf(Part(text = message)), role = "user"))

        val request = GenerateContentRequest(
            contents = chatHistory.toList(),
            generationConfig = if (useHighThinking) {
                GenerationConfig(thinkingConfig = ThinkingConfig("HIGH"))
            } else null,
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Gemileith, a highly advanced Android AI assistant within the Gemileith OS launcher. You are helpful, magical, and intelligent."))
            )
        )

        val url = "v1beta/models/$modelName:streamGenerateContent"
        
        var fullResponse = ""
        try {
            val responseBody = RetrofitClient.service.generateContentStream(url, apiKey, request)
            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    try {
                        if (line!!.startsWith(",")) line = line!!.substring(1).trim()
                        if (line!!.startsWith("[")) line = line!!.substring(1).trim()
                        if (line!!.endsWith("]")) line = line!!.dropLast(1).trim()
                        if (line!!.isEmpty()) continue
                        
                        val chunk = Json.parseToJsonElement(line!!).jsonObject
                        val text = chunk["candidates"]?.jsonArray
                            ?.getOrNull(0)?.jsonObject
                            ?.get("content")?.jsonObject
                            ?.get("parts")?.jsonArray
                            ?.getOrNull(0)?.jsonObject
                            ?.get("text")?.jsonPrimitive?.content
                        
                        if (text != null) {
                            fullResponse += text
                            emit(text)
                        }
                    } catch (e: Exception) {
                        // ignore parse errors for partial json chunks
                    }
                }
            }
            // Add model response to history
            chatHistory.add(Content(parts = listOf(Part(text = fullResponse)), role = "model"))
        } catch (e: Exception) {
            emit("\nError: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun quickAnalyze(text: String): String {
        return try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part("Analyze the intent of the following user query briefly: $text")))),
            )
            val response = RetrofitClient.service.generateContent("v1beta/models/gemini-3.1-flash-lite-preview:generateContent", apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No analysis"
        } catch (e: Exception) {
            "Analysis failed."
        }
    }
}
