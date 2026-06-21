package com.majidshahbaz.memo.android.data.cachestore

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.majidshahbaz.memo.android.cache.RoomCacheStore
import com.majidshahbaz.memo.android.cache.MemoDatabase
import com.majidshahbaz.memo.core.cache.CacheEntry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomCacheStoreTest {

    private lateinit var db: MemoDatabase
    private lateinit var store: RoomCacheStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // in-memory database — wiped after each test, never touches real disk
        db = Room.inMemoryDatabaseBuilder(context, MemoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        store = RoomCacheStore(db.cacheDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `put then get returns the same entry`() = runTest {
        val entry = CacheEntry(
            key = "abc123",
            response = "Hello cached world",
            timestamp = System.currentTimeMillis(),
            ttlSeconds = 3600,
            model = "gpt-4",
            tokenCount = 42
        )

        store.put(entry)
        val retrieved = store.get("abc123")

        assertNotNull(retrieved)
        assertEquals("Hello cached world", retrieved?.response)
        assertEquals("gpt-4", retrieved?.model)
        assertEquals(42, retrieved?.tokenCount)
    }

    @Test
    fun `get returns null for a key that was never stored`() = runTest {
        val result = store.get("does-not-exist")
        assertNull(result)
    }

    @Test
    fun `put with same key overwrites the previous entry`() = runTest {
        val first = CacheEntry("dup-key", "first answer", System.currentTimeMillis(), 3600, "gpt-4", 10)
        val second = CacheEntry("dup-key", "second answer", System.currentTimeMillis(), 3600, "gpt-4", 20)

        store.put(first)
        store.put(second)

        val result = store.get("dup-key")
        assertEquals("second answer", result?.response)
        assertEquals(20, result?.tokenCount)
    }

    @Test
    fun `evictExpired removes only expired entries`() = runTest {
        val now = System.currentTimeMillis()

        val expired = CacheEntry(
            key = "old",
            response = "stale answer",
            timestamp = now - 10_000, // 10 seconds ago
            ttlSeconds = 1,           // expired after 1 second
            model = "gpt-4",
            tokenCount = 5
        )
        val fresh = CacheEntry(
            key = "new",
            response = "fresh answer",
            timestamp = now,
            ttlSeconds = 3600,        // valid for 1 hour
            model = "gpt-4",
            tokenCount = 5
        )

        store.put(expired)
        store.put(fresh)
        store.evictExpired()

        assertNull(store.get("old"))
        assertNotNull(store.get("new"))
    }

    @Test
    fun `clear removes everything from the cache`() = runTest {
        store.put(CacheEntry("k1", "a1", System.currentTimeMillis(), 3600, "gpt-4", 1))
        store.put(CacheEntry("k2", "a2", System.currentTimeMillis(), 3600, "gpt-4", 1))

        store.clear()

        assertNull(store.get("k1"))
        assertNull(store.get("k2"))
    }

    @Test
    fun `data survives as separate object after retrieval, confirming real persistence not just memory reference`() = runTest {
        val original = CacheEntry("persist-check", "persisted value", System.currentTimeMillis(), 3600, "gpt-4", 7)
        store.put(original)

        val retrieved = store.get("persist-check")

        // confirms Room actually serialized and deserialized through SQLite,
        // not just holding the same in-memory object reference
        assertNotSame(original, retrieved)
        assertEquals(original.response, retrieved?.response)
    }
}