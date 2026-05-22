package com.example.data.repository

import com.example.data.local.dao.WaterQualityLogDao
import com.example.data.local.entity.WaterQualityLogEntity
import com.example.domain.repository.WaterQualityLogRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class WaterQualityLogRepositoryImpl(private val waterQualityLogDao: WaterQualityLogDao) : WaterQualityLogRepository {
    override suspend fun addLog(log: WaterQualityLogEntity): Long = waterQualityLogDao.insertLog(log)

    override suspend fun updateLog(log: WaterQualityLogEntity) = waterQualityLogDao.updateLog(log)

    override suspend fun deleteLog(log: WaterQualityLogEntity) = waterQualityLogDao.deleteLog(log)

    override fun getLogsByPond(pondId: Long): Flow<List<WaterQualityLogEntity>> = waterQualityLogDao.getLogsByPond(pondId)

    override fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<WaterQualityLogEntity>> =
        waterQualityLogDao.getLogsByPondInDateRange(pondId, startDate)

    override suspend fun getLatestLogByPond(pondId: Long): WaterQualityLogEntity? = waterQualityLogDao.getLatestLogByPond(pondId)

    override suspend fun getAverageTemperature(pondId: Long, startDate: LocalDateTime): Double =
        waterQualityLogDao.getAverageTemperature(pondId, startDate) ?: 0.0

    override suspend fun getAveragePH(pondId: Long, startDate: LocalDateTime): Double =
        waterQualityLogDao.getAveragePH(pondId, startDate) ?: 0.0

    override suspend fun getAverageDO(pondId: Long, startDate: LocalDateTime): Double =
        waterQualityLogDao.getAverageDO(pondId, startDate) ?: 0.0

    override suspend fun getAverageAmmonia(pondId: Long, startDate: LocalDateTime): Double =
        waterQualityLogDao.getAverageAmmonia(pondId, startDate) ?: 0.0

    override suspend fun getUnsyncedLogs(): List<WaterQualityLogEntity> = waterQualityLogDao.getUnsyncedLogs()

    override suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime) = waterQualityLogDao.markAsSynced(logId, syncTime)
}
