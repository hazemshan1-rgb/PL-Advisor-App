package com.example.data.repository

import com.example.data.local.dao.SurvivorshipSamplingDao
import com.example.data.local.entity.SurvivorshipSamplingEntity
import com.example.domain.repository.SurvivorshipSamplingRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class SurvivorshipSamplingRepositoryImpl(private val samplingDao: SurvivorshipSamplingDao) : SurvivorshipSamplingRepository {
    override suspend fun addSampling(sampling: SurvivorshipSamplingEntity): Long = samplingDao.insertSampling(sampling)

    override suspend fun updateSampling(sampling: SurvivorshipSamplingEntity) = samplingDao.updateSampling(sampling)

    override suspend fun deleteSampling(sampling: SurvivorshipSamplingEntity) = samplingDao.deleteSampling(sampling)

    override fun getSamplingsByPond(pondId: Long): Flow<List<SurvivorshipSamplingEntity>> = samplingDao.getSamplingsByPond(pondId)

    override fun getSamplingsByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<SurvivorshipSamplingEntity>> =
        samplingDao.getSamplingsByPondInDateRange(pondId, startDate)

    override suspend fun getLatestSamplingByPond(pondId: Long): SurvivorshipSamplingEntity? = samplingDao.getLatestSamplingByPond(pondId)

    override suspend fun getAveragePopulation(pondId: Long, startDate: LocalDateTime): Int =
        samplingDao.getAveragePopulation(pondId, startDate)?.toInt() ?: 0

    override suspend fun getAverageSizeG(pondId: Long, startDate: LocalDateTime): Double =
        samplingDao.getAverageSizeG(pondId, startDate) ?: 0.0
}
