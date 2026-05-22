package com.example.domain.repository

import com.example.data.local.entity.PLBatchEntity
import kotlinx.coroutines.flow.Flow

interface PLBatchRepository {
    suspend fun addBatch(batch: PLBatchEntity): Long
    suspend fun updateBatch(batch: PLBatchEntity)
    suspend fun deleteBatch(batch: PLBatchEntity)
    suspend fun getBatchById(batchId: Long): PLBatchEntity?
    fun getBatchesByPond(pondId: Long): Flow<List<PLBatchEntity>>
    suspend fun getLatestBatchByPond(pondId: Long): PLBatchEntity?
    fun getBatchesByHatchery(hatcheryName: String): Flow<List<PLBatchEntity>>
    suspend fun getAverageQualityByPond(pondId: Long): Double
}
