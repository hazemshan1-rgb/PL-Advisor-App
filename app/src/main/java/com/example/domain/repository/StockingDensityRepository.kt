package com.example.domain.repository

import com.example.data.local.entity.StockingDensityEntity
import kotlinx.coroutines.flow.Flow

interface StockingDensityRepository {
    suspend fun addDensity(density: StockingDensityEntity): Long
    suspend fun updateDensity(density: StockingDensityEntity)
    suspend fun deleteDensity(density: StockingDensityEntity)
    fun getDensitiesByPond(pondId: Long): Flow<List<StockingDensityEntity>>
    suspend fun getLatestDensityByPond(pondId: Long): StockingDensityEntity?
    suspend fun getDensityByBatch(batchId: Long): StockingDensityEntity?
    suspend fun getCriticalRiskCount(): Int
}
