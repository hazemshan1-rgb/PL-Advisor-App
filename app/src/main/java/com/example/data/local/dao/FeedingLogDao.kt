package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FeedingLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FeedingLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FeedingLogEntity): Long

    @Update
    suspend fun updateLog(log: FeedingLogEntity)

    @Delete
    suspend fun deleteLog(log: FeedingLogEntity)

    @Query("SELECT * FROM feeding_logs WHERE pondId = :pondId ORDER BY date DESC")
    fun getLogsByPond(pondId: Long): Flow<List<FeedingLogEntity>>

    @Query("SELECT * FROM feeding_logs WHERE pondId = :pondId AND date >= :startDate ORDER BY date DESC")
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<FeedingLogEntity>>

    @Query("SELECT SUM(feedQuantityKg) FROM feeding_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getTotalFeedInRange(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT SUM(feedQuantityKg * feedCostPerKg) FROM feeding_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getTotalFeedCostInRange(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT AVG(feedQuantityKg) FROM feeding_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAverageDailyFeed(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT * FROM feeding_logs WHERE syncedAt IS NULL ORDER BY date ASC")
    suspend fun getUnsyncedLogs(): List<FeedingLogEntity>

    @Query("UPDATE feeding_logs SET syncedAt = :syncTime WHERE id = :logId")
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
