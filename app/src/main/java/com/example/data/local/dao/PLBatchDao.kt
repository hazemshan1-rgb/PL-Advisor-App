package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PLBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PLBatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: PLBatchEntity): Long

    @Update
    suspend fun updateBatch(batch: PLBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: PLBatchEntity)

    @Query("SELECT * FROM pl_batches WHERE id = :batchId")
    suspend fun getBatchById(batchId: Long): PLBatchEntity?

    @Query("SELECT * FROM pl_batches WHERE pondId = :pondId ORDER BY stockingDate DESC")
    fun getBatchesByPond(pondId: Long): Flow<List<PLBatchEntity>>

    @Query("SELECT * FROM pl_batches WHERE pondId = :pondId ORDER BY stockingDate DESC LIMIT 1")
    suspend fun getLatestBatchByPond(pondId: Long): PLBatchEntity?

    @Query("SELECT * FROM pl_batches WHERE hatcheryName = :hatcheryName ORDER BY stockingDate DESC")
    fun getBatchesByHatchery(hatcheryName: String): Flow<List<PLBatchEntity>>

    @Query("SELECT AVG(qualityScore) FROM pl_batches WHERE pondId = :pondId")
    suspend fun getAverageQualityByPond(pondId: Long): Double
}
