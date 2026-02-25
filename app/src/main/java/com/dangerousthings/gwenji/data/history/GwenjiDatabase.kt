package com.dangerousthings.gwenji.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class], version = 1)
abstract class GwenjiDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: GwenjiDatabase? = null

        fun getDatabase(context: Context): GwenjiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GwenjiDatabase::class.java,
                    "gwenji_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
