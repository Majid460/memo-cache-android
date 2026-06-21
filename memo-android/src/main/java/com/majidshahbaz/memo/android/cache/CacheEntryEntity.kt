package com.majidshahbaz.memo.android.cache


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val response: String,
    val timestamp: Long,
    val ttlSeconds: Long,
    val model: String,
    val tokenCount: Int
)