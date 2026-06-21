package com.majidshahbaz.memo.core.stats

import com.majidshahbaz.memo.core.cache.MemoResponse
import com.majidshahbaz.memo.core.cache.ResponseSource
import com.majidshahbaz.memo.core.cost.CostEstimator

class StatsTracker {
    private var stats = MemoStats()

    fun record(response: MemoResponse, model: String) {
        val savedDollars = if (response.source == ResponseSource.CACHE) {
            CostEstimator.estimateSavings(model, response.tokensSaved)
        } else 0.0

        stats = stats.copy(
            totalCalls = stats.totalCalls + 1,
            cacheHits = stats.cacheHits + if (response.source == ResponseSource.CACHE) 1 else 0,
            networkCalls = stats.networkCalls + if (response.source == ResponseSource.NETWORK) 1 else 0,
            offlineFallbacks = stats.offlineFallbacks + if (response.source == ResponseSource.OFFLINE_FALLBACK) 1 else 0,
            totalDollarsSaved = stats.totalDollarsSaved + savedDollars
        )
    }

    fun getStats(): MemoStats = stats

    fun reset() {
        stats = MemoStats()
    }
}