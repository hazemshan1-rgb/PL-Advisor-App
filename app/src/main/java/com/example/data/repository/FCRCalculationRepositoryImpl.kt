package com.example.data.repository

import com.example.data.local.dao.FCRCalculationDao
import com.example.data.local.entity.FCRCalculationEntity
import com.example.domain.repository.FCRCalculationRepository
import kotlinx.coroutines.flow.Flow

class FCRCalculationRepositoryImpl(private val fcrCalculationDao: FCRCalculationDao) : FCRCalculationRepository {
    override suspend fun addCalculation(calculation: FCRCalculationEntity): Long = fcrCalculationDao.insertCalculation(calculation)

    override suspend fun updateCalculation(calculation: FCRCalculationEntity) = fcrCalculationDao.updateCalculation(calculation)

    override suspend fun deleteCalculation(calculation: FCRCalculationEntity) = fcrCalculationDao.deleteCalculation(calculation)

    override fun getCalculationsByPond(pondId: Long): Flow<List<FCRCalculationEntity>> = fcrCalculationDao.getCalculationsByPond(pondId)

    override suspend fun getActiveCycleByPond(pondId: Long): FCRCalculationEntity? = fcrCalculationDao.getActiveCycleByCycle(pondId)

    override suspend fun getAverageFCR(pondId: Long): Double = fcrCalculationDao.getAverageFCR(pondId) ?: 0.0

    override suspend fun getGlobalAverageFCR(): Double = fcrCalculationDao.getGlobalAverageFCR() ?: 0.0

    override suspend fun getLatestCalculation(pondId: Long): FCRCalculationEntity? = fcrCalculationDao.getLatestCalculation(pondId)
}
