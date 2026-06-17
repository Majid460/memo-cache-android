package com.majidshahbaz.memo.android.data.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CacheDaoTest {

    private lateinit var database: MemoDatabase
    private lateinit var dao: CacheDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MemoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.cacheDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetByKey() = runTest {
        val entity = CacheEntryEntity(
            key = "test_key",
            response = "test_response",
            timestamp = 1000L,
            ttlSeconds = 60L,
            model = "gpt-4",
            tokenCount = 10
        )
        dao.insert(entity)

        val retrieved = dao.getByKey("test_key")
        assertEquals(entity, retrieved)
    }

    @Test
    fun deleteExpired() = runTest {
        val now = 10000L
        val expired = CacheEntryEntity(
            key = "expired",
            response = "...",
            timestamp = 1000L,
            ttlSeconds = 1L, // expires at 2000L
            model = "m",
            tokenCount = 0
        )
        val valid = CacheEntryEntity(
            key = "valid",
            response = "...",
            timestamp = 9000L,
            ttlSeconds = 100L, // expires at 109000L
            model = "m",
            tokenCount = 0
        )

        dao.insert(expired)
        dao.insert(valid)

        dao.deleteExpired(now)

        assertNull(dao.getByKey("expired"))
        assertEquals(valid, dao.getByKey("valid"))
    }

    @Test
    fun clearAll() = runTest {
        dao.insert(CacheEntryEntity("k1", "r1", 0, 0, "m", 0))
        dao.insert(CacheEntryEntity("k2", "r2", 0, 0, "m", 0))

        dao.clearAll()

        assertNull(dao.getByKey("k1"))
        assertNull(dao.getByKey("k2"))
    }
}