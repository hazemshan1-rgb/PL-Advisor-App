package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WaterQualityLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface WaterQualityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterQualityLogEntity): Long

    @Update
    suspend fun updateLog(log: WaterQualityLogEntity)

    @Delete
    suspend fun deleteLog(log: WaterQualityLogEntity)

    @Query("SELECT * FROM water_quality_logs WHERE pondId = :pondId ORDER BY date DESC")
    fun getLogsByPond(pondId: Long): Flow<List<WaterQualityLogEntity>>

    @Query("SELECT * FROM water_quality_logs WHERE pondId = :pondId AND date >= :startDate ORDER BY date DESC")
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<WaterQualityLogEntity>>

    @Query("SELECT * FROM water_quality_logs WHERE pondId = :pondId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestLogByPond(pondId: Long): WaterQualityLogEntity?

    @Query("SELECT AVG(temperature) FROM water_quality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAverageTemperature(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT AVG(ph) FROM water_quality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAveragePH(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT AVG(dissolvedOxygen) FROM water_quality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAverageDO(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT AVG(ammonia) FROM water_quality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAverageAmmonia(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT * FROM water_quality_logs WHERE syncedAt IS NULL ORDER BY date ASC")
    suspend fun getUnsyncedLogs(): List<WaterQualityLogEntity>

    @Query("UPDATE water_quality_logs SET syncedAt = :syncTime WHERE id = :logId")
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
