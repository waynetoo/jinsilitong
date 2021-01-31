package com.waynetoo.videotv.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.waynetoo.videotv.room.dao.AdDao
import com.waynetoo.videotv.room.entity.AdInfo

@Database(
    entities = [AdInfo::class],
    version = 5
)
abstract class AdDatabase : RoomDatabase() {

    abstract fun adDao(): AdDao

    companion object {
        @Volatile
        private var INSTANCE: AdDatabase? = null

        fun getDatabase(context: Context): AdDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdDatabase::class.java,
                    "ad_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
