package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converter.LocalDateTimeConverter
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        PondEntity::class,
        PLBatchEntity::class,
        PLQualityChecklistEntity::class,
        MortalityLogEntity::class,
        SurvivorshipSamplingEntity::class,
        WaterQualityLogEntity::class,
        FeedingLogEntity::class,
        BiomassEstimateEntity::class,
        FCRCalculationEntity::class,
        StockingDensityEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pondDao(): PondDao
    abstract fun plBatchDao(): PLBatchDao
    abstract fun plQualityChecklistDao(): PLQualityChecklistDao
    abstract fun mortalityLogDao(): MortalityLogDao
    abstract fun survivorshipSamplingDao(): SurvivorshipSamplingDao
    abstract fun waterQualityLogDao(): WaterQualityLogDao
    abstract fun feedingLogDao(): FeedingLogDao
    abstract fun biomassEstimateDao(): BiomassEstimateDao
    abstract fun fcrCalculationDao(): FCRCalculationDao
    abstract fun stockingDensityDao(): StockingDensityDao

    companion object {
        private const val DATABASE_NAME = "pl_advisor_app.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development; use migrations in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
