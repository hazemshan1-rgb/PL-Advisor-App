package com.example.data.repository

import com.example.data.local.dao.PLBatchDao
import com.example.data.local.entity.PLBatchEntity
import com.example.domain.repository.PLBatchRepository
import kotlinx.coroutines.flow.Flow

class PLBatchRepositoryImpl(private val plBatchDao: PLBatchDao) : PLBatchRepository {
    override suspend fun addBatch(batch: PLBatchEntity): Long = plBatchDao.insertBatch(batch)

    override suspend fun updateBatch(batch: PLBatchEntity) = plBatchDao.updateBatch(batch)

    override suspend fun deleteBatch(batch: PLBatchEntity) = plBatchDao.deleteBatch(batch)

    override suspend fun getBatchById(batchId: Long): PLBatchEntity? = plBatchDao.getBatchById(batchId)

    override fun getBatchesByPond(pondId: Long): Flow<List<PLBatchEntity>> = plBatchDao.getBatchesByPond(pondId)

    override suspend fun getLatestBatchByPond(pondId: Long): PLBatchEntity? = plBatchDao.getLatestBatchByPond(pondId)

    override fun getBatchesByHatchery(hatcheryName: String): Flow<List<PLBatchEntity>> = plBatchDao.getBatchesByHatchery(hatcheryName)

    override suspend fun getAverageQualityByPond(pondId: Long): Double = plBatchDao.getAverageQualityByPond(pondId) ?: 0.0
}
