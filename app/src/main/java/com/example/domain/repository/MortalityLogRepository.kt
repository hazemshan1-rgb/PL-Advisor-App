package com.example.domain.repository

import com.example.data.local.entity.MortalityLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MortalityLogRepository {
    suspend fun addLog(log: MortalityLogEntity): Long
    suspend fun updateLog(log: MortalityLogEntity)
    suspend fun deleteLog(log: MortalityLogEntity)
    fun getLogsByPond(pondId: Long): Flow<List<MortalityLogEntity>>
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<MortalityLogEntity>>
    suspend fun getTotalMortalityInRange(pondId: Long, startDate: LocalDateTime): Int
    suspend fun getAverageDailyMortality(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getUnsyncedLogs(): List<MortalityLogEntity>
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
