package com.example.data.repository

import com.example.data.local.dao.MortalityLogDao
import com.example.data.local.entity.MortalityLogEntity
import com.example.domain.repository.MortalityLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class MortalityLogRepositoryImpl(private val mortalityLogDao: MortalityLogDao) : MortalityLogRepository {
    override suspend fun addLog(log: MortalityLogEntity): Long = mortalityLogDao.insertLog(log)

    override suspend fun updateLog(log: MortalityLogEntity) = mortalityLogDao.updateLog(log)

    override suspend fun deleteLog(log: MortalityLogEntity) = mortalityLogDao.deleteLog(log)

    override fun getLogsByPond(pondId: Long): Flow<List<MortalityLogEntity>> = mortalityLogDao.getLogsByPond(pondId)

    override fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<MortalityLogEntity>> =
        mortalityLogDao.getLogsByPondInDateRange(pondId, startDate)

    override suspend fun getTotalMortalityInRange(pondId: Long, startDate: LocalDateTime): Int =
        mortalityLogDao.getTotalMortalityInRange(pondId, startDate) ?: 0

    override suspend fun getAverageDailyMortality(pondId: Long, startDate: LocalDateTime): Double =
        mortalityLogDao.getAverageDailyMortality(pondId, startDate) ?: 0.0

    override suspend fun getUnsyncedLogs(): List<MortalityLogEntity> = mortalityLogDao.getUnsyncedLogs()

    override suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime) = mortalityLogDao.markAsSynced(logId, syncTime)
}
