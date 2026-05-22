package com.example.data.repository

import com.example.data.local.dao.FeedingLogDao
import com.example.data.local.entity.FeedingLogEntity
import com.example.domain.repository.FeedingLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class FeedingLogRepositoryImpl(private val feedingLogDao: FeedingLogDao) : FeedingLogRepository {
    override suspend fun addLog(log: FeedingLogEntity): Long = feedingLogDao.insertLog(log)

    override suspend fun updateLog(log: FeedingLogEntity) = feedingLogDao.updateLog(log)

    override suspend fun deleteLog(log: FeedingLogEntity) = feedingLogDao.deleteLog(log)

    override fun getLogsByPond(pondId: Long): Flow<List<FeedingLogEntity>> = feedingLogDao.getLogsByPond(pondId)

    override fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<FeedingLogEntity>> =
        feedingLogDao.getLogsByPondInDateRange(pondId, startDate)

    override suspend fun getTotalFeedInRange(pondId: Long, startDate: LocalDateTime): Double =
        feedingLogDao.getTotalFeedInRange(pondId, startDate) ?: 0.0

    override suspend fun getTotalFeedCostInRange(pondId: Long, startDate: LocalDateTime): Double =
        feedingLogDao.getTotalFeedCostInRange(pondId, startDate) ?: 0.0

    override suspend fun getAverageDailyFeed(pondId: Long, startDate: LocalDateTime): Double =
        feedingLogDao.getAverageDailyFeed(pondId, startDate) ?: 0.0

    override suspend fun getUnsyncedLogs(): List<FeedingLogEntity> = feedingLogDao.getUnsyncedLogs()

    override suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime) = feedingLogDao.markAsSynced(logId, syncTime)
}
