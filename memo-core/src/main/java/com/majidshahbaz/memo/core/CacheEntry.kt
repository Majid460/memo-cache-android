package com.majidshahbaz.memo.core

data class CacheEntry(
    val key: String,
    val response: String,
    val timestamp: Long,
    val ttlSeconds: Long,
    val model: String,
    val tokenCount: Int = 0
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return (now - timestamp) > (ttlSeconds * 1000)
    }
}