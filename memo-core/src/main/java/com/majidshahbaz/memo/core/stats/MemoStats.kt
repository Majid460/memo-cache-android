package com.majidshahbaz.memo.core.stats

import com.majidshahbaz.memo.core.cache.MemoResponse
import com.majidshahbaz.memo.core.cache.ResponseSource
import com.majidshahbaz.memo.core.cost.CostEstimator

data class MemoStats(
    val totalCalls: Int = 0,
    val cacheHits: Int = 0,
    val networkCalls: Int = 0,
    val offlineFallbacks: Int = 0,
    val totalDollarsSaved: Double = 0.0
) {
    val cacheHitRate: Double
        get() = if (totalCalls == 0) 0.0 else (cacheHits.toDouble() / totalCalls) * 100
}

