package com.majidshahbaz.memo.core

import com.majidshahbaz.memo.core.cache.CacheEntry
import com.majidshahbaz.memo.core.cache.CacheKeyGenerator
import com.majidshahbaz.memo.core.cache.CacheStore
import com.majidshahbaz.memo.core.costanalyzer.CostEstimator
import com.majidshahbaz.memo.core.costanalyzer.MemoStats
import com.majidshahbaz.memo.core.costanalyzer.StatsTracker


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
}