package com.majidshahbaz.memo.core.cache

import com.majidshahbaz.memo.core.cost.CostEstimator
import com.majidshahbaz.memo.core.stats.MemoStats
import com.majidshahbaz.memo.core.stats.StatsTracker


enum class ResponseSource { CACHE, NETWORK, OFFLINE_FALLBACK }

data class MemoResponse(
    val text: String,
    val source: ResponseSource,
    val tokensSaved: Int = 0
)

class MemoCache(
    private val store: CacheStore,
    private val statsTracker: StatsTracker = StatsTracker()
) {

    suspend fun call(
        prompt: String,
        model: String,
        ttlSeconds: Long = 86400,
        apiCall: suspend () -> String
    ): MemoResponse {
        val key = CacheKeyGenerator.generate(prompt, model)
        val cached = store.get(key)

        val response = if (cached != null && !cached.isExpired()) {
            MemoResponse(
                text = cached.response,
                source = ResponseSource.CACHE,
                tokensSaved = cached.tokenCount
            )
        } else {
            val freshResponse = apiCall()
            val tokenCount = CostEstimator.estimateTokenCount(freshResponse)
            store.put(
                CacheEntry(
                    key = key,
                    response = freshResponse,
                    timestamp = System.currentTimeMillis(),
                    ttlSeconds = ttlSeconds,
                    model = model,
                    tokenCount = tokenCount
                )
            )
            MemoResponse(text = freshResponse, source = ResponseSource.NETWORK)
        }

        statsTracker.record(response, model)
        return response
    }

    fun getStats(): MemoStats = statsTracker.getStats()

    /**
     * Checks the cache for an existing, non-expired entry without
     * triggering a network/fallback call on miss. Returns null if
     * there's no valid-cached entry.
     */
    suspend fun peek(prompt: String, model: String): String? {
        val key = CacheKeyGenerator.generate(prompt, model)
        val cached = store.get(key)
        return if (cached != null && !cached.isExpired()) {
            cached.response
        } else {
            null
        }
    }
}