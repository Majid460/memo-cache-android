package com.majidshahbaz.memo.android.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [CacheEntryEntity::class], version = 1, exportSchema = false)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var INSTANCE: MemoDatabase? = null

        fun getInstance(context: Context): MemoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MemoDatabase::class.java,
                    "memo_cache_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}