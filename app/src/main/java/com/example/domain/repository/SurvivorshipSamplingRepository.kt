package com.example.domain.repository

import com.example.data.local.entity.SurvivorshipSamplingEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface SurvivorshipSamplingRepository {
    suspend fun addSampling(sampling: SurvivorshipSamplingEntity): Long
    suspend fun updateSampling(sampling: SurvivorshipSamplingEntity)
    suspend fun deleteSampling(sampling: SurvivorshipSamplingEntity)
    fun getSamplingsByPond(pondId: Long): Flow<List<SurvivorshipSamplingEntity>>
    fun getSamplingsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<SurvivorshipSamplingEntity>>
    suspend fun getLatestSamplingByPond(pondId: Long): SurvivorshipSamplingEntity?
    suspend fun getAveragePopulation(pondId: Long, startDate: LocalDateTime): Int
    suspend fun getAverageSizeG(pondId: Long, startDate: LocalDateTime): Double
}
