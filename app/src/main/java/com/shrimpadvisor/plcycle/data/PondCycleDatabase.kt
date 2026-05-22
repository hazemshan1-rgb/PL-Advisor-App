package com.shrimpadvisor.plcycle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PondCycle::class, DailyReading::class, RegionProfile::class], version = 4, exportSchema = false)
abstract class PondCycleDatabase : RoomDatabase() {
    abstract fun pondCycleDao(): PondCycleDao
    abstract fun dailyReadingDao(): DailyReadingDao
    abstract fun regionProfileDao(): RegionProfileDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create region_profiles table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `region_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `regionName` TEXT NOT NULL,
                        `currency` TEXT NOT NULL DEFAULT 'USD',
                        `priceAt20g` REAL NOT NULL DEFAULT 3.5,
                        `priceAt25g` REAL NOT NULL DEFAULT 4.0,
                        `priceAt30g` REAL NOT NULL DEFAULT 5.0,
                        `priceAt35g` REAL NOT NULL DEFAULT 6.0,
                        `priceAt40g` REAL NOT NULL DEFAULT 7.0,
                        `feedCostDefault` REAL NOT NULL DEFAULT 1.25,
                        `laborCostDefault` REAL NOT NULL DEFAULT 4.0,
                        `aerationCostDefault` REAL NOT NULL DEFAULT 8.0,
                        `probioticCostDefault` REAL NOT NULL DEFAULT 3.0,
                        `isBuiltIn` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                // Insert 4 built-in regions
                database.execSQL(
                    """
                    INSERT INTO `region_profiles`
                        (`regionName`, `currency`, `priceAt20g`, `priceAt25g`, `priceAt30g`, `priceAt35g`, `priceAt40g`,
                         `feedCostDefault`, `laborCostDefault`, `aerationCostDefault`, `probioticCostDefault`, `isBuiltIn`)
                    VALUES
                        ('Vietnam',      'USD', 3.0, 3.8, 4.5, 5.5, 6.5,  1.10, 3.0, 7.0, 2.5, 1),
                        ('Indonesia',    'USD', 3.2, 4.0, 4.8, 5.8, 6.8,  1.15, 3.5, 7.5, 2.8, 1),
                        ('Saudi Arabia', 'USD', 5.0, 6.0, 7.5, 9.0, 10.5, 1.60, 6.0, 12.0, 4.0, 1),
                        ('Generic',      'USD', 3.5, 4.0, 5.0, 6.0, 7.0,  1.25, 4.0, 8.0, 3.0, 1)
                    """.trimIndent()
                )

                // Add regionProfileId column to pond_cycles
                database.execSQL(
                    "ALTER TABLE `pond_cycles` ADD COLUMN `regionProfileId` INTEGER DEFAULT NULL"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE `daily_readings` ADD COLUMN `feedGiven` REAL NOT NULL DEFAULT 0.0"
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
