package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PondCycle::class], version = 1, exportSchema = false)
abstract class PondCycleDatabase : RoomDatabase() {
    abstract fun pondCycleDao(): PondCycleDao

    companion object {
        @Volatile
        private var INSTANCE: PondCycleDatabase? = null

        fun getDatabase(context: Context): PondCycleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PondCycleDatabase::class.java,
                    "shrimp_pond_cycles_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
