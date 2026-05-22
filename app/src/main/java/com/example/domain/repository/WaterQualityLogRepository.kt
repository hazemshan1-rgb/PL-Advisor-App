package com.example.domain.repository

import com.example.data.local.entity.WaterQualityLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface WaterQualityLogRepository {
    suspend fun addLog(log: WaterQualityLogEntity): Long
    suspend fun updateLog(log: WaterQualityLogEntity)
    suspend fun deleteLog(log: WaterQualityLogEntity)
    fun getLogsByPond(pondId: Long): Flow<List<WaterQualityLogEntity>>
    fun getLogsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<WaterQualityLogEntity>>
    suspend fun getLatestLogByPond(pondId: Long): WaterQualityLogEntity?
    suspend fun getAverageTemperature(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getAveragePH(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getAverageDO(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getAverageAmmonia(pondId: Long, startDate: LocalDateTime): Double
    suspend fun getUnsyncedLogs(): List<WaterQualityLogEntity>
    suspend fun markAsSynced(logId: Long, syncTime: LocalDateTime)
}
