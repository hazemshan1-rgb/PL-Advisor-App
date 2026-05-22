package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SurvivorshipSamplingEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SurvivorshipSamplingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSampling(sampling: SurvivorshipSamplingEntity): Long

    @Update
    suspend fun updateSampling(sampling: SurvivorshipSamplingEntity)

    @Delete
    suspend fun deleteSampling(sampling: SurvivorshipSamplingEntity)

    @Query("SELECT * FROM survivorship_samplings WHERE pondId = :pondId ORDER BY samplingDate DESC")
    fun getSamplingsByPond(pondId: Long): Flow<List<SurvivorshipSamplingEntity>>

    @Query("SELECT * FROM survivorship_samplings WHERE pondId = :pondId AND samplingDate >= :startDate ORDER BY samplingDate DESC")
    fun getSamplingsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<SurvivorshipSamplingEntity>>

    @Query("SELECT * FROM survivorship_samplings WHERE pondId = :pondId ORDER BY samplingDate DESC LIMIT 1")
    suspend fun getLatestSamplingByPond(pondId: Long): SurvivorshipSamplingEntity?

    @Query("SELECT AVG(estimatedPopulation) FROM survivorship_samplings WHERE pondId = :pondId AND samplingDate >= :startDate")
    suspend fun getAveragePopulation(pondId: Long, startDate: LocalDateTime): Int

    @Query("SELECT AVG(averageSizeG) FROM survivorship_samplings WHERE pondId = :pondId AND samplingDate >= :startDate")
    suspend fun getAverageSizeG(pondId: Long, startDate: LocalDateTime): Double
}
