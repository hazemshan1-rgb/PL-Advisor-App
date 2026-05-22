package com.example.data.repository

import com.example.data.local.dao.BiomassEstimateDao
import com.example.data.local.entity.BiomassEstimateEntity
import com.example.domain.repository.BiomassEstimateRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class BiomassEstimateRepositoryImpl(private val biomassEstimateDao: BiomassEstimateDao) : BiomassEstimateRepository {
    override suspend fun addEstimate(estimate: BiomassEstimateEntity): Long = biomassEstimateDao.insertEstimate(estimate)

    override suspend fun updateEstimate(estimate: BiomassEstimateEntity) = biomassEstimateDao.updateEstimate(estimate)

    override suspend fun deleteEstimate(estimate: BiomassEstimateEntity) = biomassEstimateDao.deleteEstimate(estimate)

    override fun getEstimatesByPond(pondId: Long): Flow<List<BiomassEstimateEntity>> = biomassEstimateDao.getEstimatesByPond(pondId)

    override fun getEstimatesByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<BiomassEstimateEntity>> =
        biomassEstimateDao.getEstimatesByPondInDateRange(pondId, startDate)

    override suspend fun getLatestEstimateByPond(pondId: Long): BiomassEstimateEntity? = biomassEstimateDao.getLatestEstimateByPond(pondId)

    override suspend fun getAverageBiomass(pondId: Long, startDate: LocalDateTime): Double =
        biomassEstimateDao.getAverageBiomass(pondId, startDate) ?: 0.0
}
