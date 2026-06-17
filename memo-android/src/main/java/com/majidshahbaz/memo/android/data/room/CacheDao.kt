package com.majidshahbaz.memo.android.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache_entries WHERE `key` = :key")
    suspend fun getByKey(key: String): CacheEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntryEntity)

    @Query("DELETE FROM cache_entries WHERE (:now - timestamp) > (ttlSeconds * 1000)")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM cache_entries")
    suspend fun clearAll()
}