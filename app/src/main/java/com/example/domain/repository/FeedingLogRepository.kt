package com.example.domain.repository

import com.example.data.local.entity.FeedingLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface FeedingLogRepository {
    suspend fun addLog(log: FeedingLogEntity): Long
    suspend fun updateLog(log: FeedingLogEntity)
    suspend fun deleteLog(log: FeedingLogEntity)
    fun getLogsByPond(pondId: Long): Flow<List<FeedingLogEntity>>
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<FeedingLogEntity>>
    suspend fun getTotalFeedInRange(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getTotalFeedCostInRange(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getAverageDailyFeed(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getUnsyncedLogs(): List<FeedingLogEntity>
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
