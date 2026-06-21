package com.majidshahbaz.memo.core

import com.majidshahbaz.memo.core.cache.MemoCache
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class MemoCacheStatsIntegrationTest {

    @Test
    fun `repeated identical calls accumulate savings in getStats`() = runTest {
        val store = MemoCacheTest.FakeCacheStore()
        val memo = MemoCache(store)

        memo.call(prompt = "What is Kotlin?", model = "gpt-4") { "Kotlin is a programming language." }
        memo.call(prompt = "What is Kotlin?", model = "gpt-4") { "Kotlin is a programming language." }
        memo.call(prompt = "What is Kotlin?", model = "gpt-4") { "Kotlin is a programming language." }

        val stats = memo.getStats()

        assertEquals(3, stats.totalCalls)
        assertEquals(1, stats.networkCalls)
        assertEquals(2, stats.cacheHits)
        assertTrue(stats.totalDollarsSaved > 0.0)
    }
}