package com.majidshahbaz.memo.core

import com.majidshahbaz.memo.core.cache.MemoResponse
import com.majidshahbaz.memo.core.cache.ResponseSource
import com.majidshahbaz.memo.core.stats.StatsTracker

import org.junit.Test
import org.junit.Assert.*

class StatsTrackerTest {

    @Test
    fun `recording a network call increments totalCalls and networkCalls only`() {
        val tracker = StatsTracker()
        val response = MemoResponse(text = "answer", source = ResponseSource.NETWORK)

        tracker.record(response, "gpt-4")
        val stats = tracker.getStats()

        assertEquals(1, stats.totalCalls)
        assertEquals(1, stats.networkCalls)
        assertEquals(0, stats.cacheHits)
        assertEquals(0.0, stats.totalDollarsSaved, 0.0001)
    }

    @Test
    fun `recording a cache hit increases dollars saved`() {
        val tracker = StatsTracker()
        val response =
            MemoResponse(text = "cached answer", source = ResponseSource.CACHE, tokensSaved = 1000)

        tracker.record(response, "gpt-4")
        val stats = tracker.getStats()

        assertEquals(1, stats.cacheHits)
        assertTrue(stats.totalDollarsSaved > 0.0)
        assertEquals(0.03, stats.totalDollarsSaved, 0.0001)
    }

    @Test
    fun `cache hit rate calculates correctly across mixed calls`() {
        val tracker = StatsTracker()

        tracker.record(MemoResponse("a", ResponseSource.NETWORK), "gpt-4")
        tracker.record(MemoResponse("b", ResponseSource.CACHE, tokensSaved = 500), "gpt-4")
        tracker.record(MemoResponse("c", ResponseSource.CACHE, tokensSaved = 500), "gpt-4")
        tracker.record(MemoResponse("d", ResponseSource.OFFLINE_FALLBACK), "gpt-4")

        val stats = tracker.getStats()

        assertEquals(4, stats.totalCalls)
        assertEquals(2, stats.cacheHits)
        assertEquals(1, stats.networkCalls)
        assertEquals(1, stats.offlineFallbacks)
        assertEquals(50.0, stats.cacheHitRate, 0.0001) // 2 out of 4 = 50%
    }

    @Test
    fun `hit rate is zero when no calls have been recorded yet`() {
        val tracker = StatsTracker()
        assertEquals(0.0, tracker.getStats().cacheHitRate, 0.0001)
    }

    @Test
    fun `reset clears all accumulated stats back to zero`() {
        val tracker = StatsTracker()
        tracker.record(MemoResponse("a", ResponseSource.CACHE, tokensSaved = 1000), "gpt-4")

        tracker.reset()
        val stats = tracker.getStats()

        assertEquals(0, stats.totalCalls)
        assertEquals(0, stats.cacheHits)
        assertEquals(0.0, stats.totalDollarsSaved, 0.0001)
    }
}