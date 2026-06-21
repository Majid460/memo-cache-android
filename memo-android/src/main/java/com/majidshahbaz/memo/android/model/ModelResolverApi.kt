package com.majidshahbaz.memo.android.model

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ResolveModelRequest(
    val totalRamMb: Long,
    val availableStorageMb: Long,
    val requestedTier: String? = null
)

@Serializable
data class ResolvedModel(
    val tier: String,
    val modelName: String,
    val fileName: String,
    val sizeMb: Int,
    val downloadUrl: String
)

class ModelResolverApi(
    private val resolverEndpoint: String
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun resolveModel(
        totalRamMb: Long,
        availableStorageMb: Long,
        requestedTier: String? = null
    ): ResolvedModel? {
        return try {
            val response = client.post(resolverEndpoint) {
                contentType(ContentType.Application.Json)
                setBody(ResolveModelRequest(totalRamMb, availableStorageMb, requestedTier))
            }
            Json { ignoreUnknownKeys = true }
                .decodeFromString<ResolvedModel>(response.bodyAsText())
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        client.close()
    }
}