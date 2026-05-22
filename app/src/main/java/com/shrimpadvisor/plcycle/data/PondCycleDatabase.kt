package com.shrimpadvisor.plcycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PondCycle::class, DailyReading::class], version = 2, exportSchema = false)
abstract class PondCycleDatabase : RoomDatabase() {
    abstract fun pondCycleDao(): PondCycleDao
    abstract fun dailyReadingDao(): DailyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: PondCycleDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `daily_readings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pondCycleId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `pondAge` INTEGER NOT NULL,
                        `survivalPct` REAL NOT NULL,
                        `doLevel` REAL NOT NULL,
                        `tanLevel` REAL NOT NULL,
                        `ph` REAL NOT NULL,
                        `temp` REAL NOT NULL,
                        `abw` REAL NOT NULL,
                        `notes` TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(`pondCycleId`) REFERENCES `pond_cycles`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_daily_readings_pondCycleId` ON `daily_readings` (`pondCycleId`)"
                )
            }
        }

        fun getDatabase(context: Context): PondCycleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PondCycleDatabase::class.java,
                    "shrimp_pond_cycles_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
