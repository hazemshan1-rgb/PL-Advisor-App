package com.example.data.repository

import com.example.data.local.dao.StockingDensityDao
import com.example.data.local.entity.StockingDensityEntity
import com.example.domain.repository.StockingDensityRepository
import kotlinx.coroutines.flow.Flow

class StockingDensityRepositoryImpl(private val stockingDensityDao: StockingDensityDao) : StockingDensityRepository {
    override suspend fun addDensity(density: StockingDensityEntity): Long = stockingDensityDao.insertDensity(density)

    override suspend fun updateDensity(density: StockingDensityEntity) = stockingDensityDao.updateDensity(density)

    override suspend fun deleteDensity(density: StockingDensityEntity) = stockingDensityDao.deleteDensity(density)

    override fun getDensitiesByPond(pondId: Long): Flow<List<StockingDensityEntity>> = stockingDensityDao.getDensitiesByPond(pondId)

    override suspend fun getLatestDensityByPond(pondId: Long): StockingDensityEntity? = stockingDensityDao.getLatestDensityByPond(pondId)

    override suspend fun getDensityByBatch(batchId: Long): StockingDensityEntity? = stockingDensityDao.getDensityByBatch(batchId)

    override suspend fun getCriticalRiskCount(): Int = stockingDensityDao.getCriticalRiskCount()
}
