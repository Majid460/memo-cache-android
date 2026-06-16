package com.majidshahbaz.memo.core


enum class ResponseSource { CACHE, NETWORK, OFFLINE_FALLBACK }

data class MemoResponse(
    val text: String,
    val source: ResponseSource,
    val tokensSaved: Int = 0
)

class MemoCache(private val store: CacheStore) {

    suspend fun call(
        prompt: String,
        model: String,
        ttlSeconds: Long = 86400, // default 24h
        apiCall: suspend () -> String
    ): MemoResponse {
        val key = CacheKeyGenerator.generate(prompt, model)
        val cached = store.get(key)

        if (cached != null && !cached.isExpired()) {
            return MemoResponse(
                text = cached.response,
                source = ResponseSource.CACHE,
                tokensSaved = cached.tokenCount
            )
        }

        val freshResponse = apiCall()
        store.put(
            CacheEntry(
                key = key,
                response = freshResponse,
                timestamp = System.currentTimeMillis(),
                ttlSeconds = ttlSeconds,
                model = model
            )
        )

        return MemoResponse(text = freshResponse, source = ResponseSource.NETWORK)
    }
}