package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.StockingDensityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockingDensityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDensity(density: StockingDensityEntity): Long

    @Update
    suspend fun updateDensity(density: StockingDensityEntity)

    @Delete
    suspend fun deleteDensity(density: StockingDensityEntity)

    @Query("SELECT * FROM stocking_densities WHERE pondId = :pondId ORDER BY calculatedAt DESC")
    fun getDensitiesByPond(pondId: Long): Flow<List<StockingDensityEntity>>

    @Query("SELECT * FROM stocking_densities WHERE pondId = :pondId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestDensityByPond(pondId: Long): StockingDensityEntity?

    @Query("SELECT * FROM stocking_densities WHERE batchId = :batchId")
    suspend fun getDensityByBatch(batchId: Long): StockingDensityEntity?

    @Query("SELECT COUNT(*) FROM stocking_densities WHERE riskLevel = 'critical'")
    suspend fun getCriticalRiskCount(): Int
}
