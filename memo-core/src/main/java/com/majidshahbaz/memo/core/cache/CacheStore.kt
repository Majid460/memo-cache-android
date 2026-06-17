package com.majidshahbaz.memo.core.cache

interface CacheStore {
    suspend fun get(key: String): CacheEntry?
    suspend fun put(entry: CacheEntry)
    suspend fun evictExpired()
    suspend fun clear()
}