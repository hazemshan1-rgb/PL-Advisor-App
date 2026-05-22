package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MortalityLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MortalityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MortalityLogEntity): Long

    @Update
    suspend fun updateLog(log: MortalityLogEntity)

    @Delete
    suspend fun deleteLog(log: MortalityLogEntity)

    @Query("SELECT * FROM mortality_logs WHERE pondId = :pondId ORDER BY date DESC")
    fun getLogsByPond(pondId: Long): Flow<List<MortalityLogEntity>>

    @Query("SELECT * FROM mortality_logs WHERE pondId = :pondId AND date >= :startDate ORDER BY date DESC")
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<MortalityLogEntity>>

    @Query("SELECT SUM(mortalityCount) FROM mortality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getTotalMortalityInRange(pondId: Long, startDate: LocalDateTime): Int

    @Query("SELECT AVG(mortalityCount) FROM mortality_logs WHERE pondId = :pondId AND date >= :startDate")
    suspend fun getAverageDailyMortality(pondId: Long, startDate: LocalDateTime): Double

    @Query("SELECT * FROM mortality_logs WHERE syncedAt IS NULL ORDER BY date ASC")
    suspend fun getUnsyncedLogs(): List<MortalityLogEntity>

    @Query("UPDATE mortality_logs SET syncedAt = :syncTime WHERE id = :logId")
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
