package com.majidshahbaz.memo.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7
)
@Serializable
data class GroqChoice(
    val message: GroqMessage
)

@Serializable
data class GroqChatResponse(
    val choices: List<GroqChoice>
)

class GroqApiClient(private val apiKey: String) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun chat(prompt: String, model: String = "llama-3.3-70b-versatile"): String {
        val request = GroqChatRequest(
            model = model,
            messages = listOf(GroqMessage(role = "user", content = prompt))
        )

        val response = client.post("https://api.groq.com/openai/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            setBody(request)
        }

        val rawBody = response.bodyAsText()
        android.util.Log.d("GroqApiClient", "RAW RESPONSE: $rawBody")  // ← temporary debug line

        val parsed = Json { ignoreUnknownKeys = true }
            .decodeFromString<GroqChatResponse>(rawBody)

        return parsed.choices.firstOrNull()?.message?.content
            ?: "No response received from Groq."
    }

    fun close() {
        client.close()
    }
}
