package com.majidshahbaz.memo.android.data.cachestore

import com.majidshahbaz.memo.android.data.room.CacheDao
import com.majidshahbaz.memo.android.data.room.CacheEntryEntity

import com.majidshahbaz.memo.core.cache.CacheEntry
import com.majidshahbaz.memo.core.cache.CacheStore

class RoomCacheStore(private val dao: CacheDao) : CacheStore {

    override suspend fun get(key: String): CacheEntry? {
        val entity = dao.getByKey(key) ?: return null
        return CacheEntry(
            key = entity.key,
            response = entity.response,
            timestamp = entity.timestamp,
            ttlSeconds = entity.ttlSeconds,
            model = entity.model,
            tokenCount = entity.tokenCount
        )
    }

    override suspend fun put(entry: CacheEntry) {
        dao.insert(
            CacheEntryEntity(
                key = entry.key,
                response = entry.response,
                timestamp = entry.timestamp,
                ttlSeconds = entry.ttlSeconds,
                model = entry.model,
                tokenCount = entry.tokenCount
            )
        )
    }

    override suspend fun evictExpired() {
        dao.deleteExpired(System.currentTimeMillis())
    }

    override suspend fun clear() {
        dao.clearAll()
    }
}