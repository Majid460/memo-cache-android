package com.majidshahbaz.memo.core

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class MemoCacheTest {

    // Fake in-memory store for testing (no Room needed yet)
    class FakeCacheStore : CacheStore {
        private val map = mutableMapOf<String, CacheEntry>()

        override suspend fun get(key: String): CacheEntry? = map[key]
        override suspend fun put(entry: CacheEntry) { map[entry.key] = entry }
        override suspend fun evictExpired() {
            map.entries.removeAll { it.value.isExpired() }
        }
        override suspend fun clear() = map.clear()
    }

    @Test
    fun `same prompt and model generate same cache key`() {
        val key1 = CacheKeyGenerator.generate("Summarize this", "gpt-4")
        val key2 = CacheKeyGenerator.generate("Summarize this", "gpt-4")
        assertEquals(key1, key2)
    }

    @Test
    fun `different prompts generate different cache keys`() {
        val key1 = CacheKeyGenerator.generate("Summarize this", "gpt-4")
        val key2 = CacheKeyGenerator.generate("Translate this", "gpt-4")
        assertNotEquals(key1, key2)
    }

    @Test
    fun `prompt is normalized so casing and whitespace dont change the key`() {
        val key1 = CacheKeyGenerator.generate("  Summarize THIS  ", "gpt-4")
        val key2 = CacheKeyGenerator.generate("summarize this", "gpt-4")
        assertEquals(key1, key2)
    }

    @Test
    fun `first call hits network and second identical call hits cache`() = runTest {
        val store = FakeCacheStore()
        val memo = MemoCache(store)
        var apiCallCount = 0

        val first = memo.call(prompt = "Hello world", model = "gpt-4") {
            apiCallCount++
            "Hi there!"
        }
        val second = memo.call(prompt = "Hello world", model = "gpt-4") {
            apiCallCount++
            "Hi there!"
        }

        assertEquals(ResponseSource.NETWORK, first.source)
        assertEquals(ResponseSource.CACHE, second.source)
        assertEquals(1, apiCallCount) // API should only be called once
    }

    @Test
    fun `expired cache entry is treated as a miss`() = runTest {
        val store = FakeCacheStore()
        val memo = MemoCache(store)

        // Insert an already-expired entry manually
        store.put(
            CacheEntry(
                key = CacheKeyGenerator.generate("old question", "gpt-4"),
                response = "old answer",
                timestamp = System.currentTimeMillis() - 100_000,
                ttlSeconds = 1, // expires after 1 second
                model = "gpt-4"
            )
        )

        var apiCalled = false
        val result = memo.call(prompt = "old question", model = "gpt-4", ttlSeconds = 1) {
            apiCalled = true
            "fresh answer"
        }

        assertTrue(apiCalled)
        assertEquals(ResponseSource.NETWORK, result.source)
        assertEquals("fresh answer", result.text)
    }
}