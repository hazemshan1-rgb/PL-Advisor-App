package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FCRCalculationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FCRCalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: FCRCalculationEntity): Long

    @Update
    suspend fun updateCalculation(calculation: FCRCalculationEntity)

    @Delete
    suspend fun deleteCalculation(calculation: FCRCalculationEntity)

    @Query("SELECT * FROM fcr_calculations WHERE pondId = :pondId ORDER BY cycleStartDate DESC")
    fun getCalculationsByPond(pondId: Long): Flow<List<FCRCalculationEntity>>

    @Query("SELECT * FROM fcr_calculations WHERE pondId = :pondId AND cycleEndDate IS NULL")
    suspend fun getActiveCycleByCycle(pondId: Long): FCRCalculationEntity?

    @Query("SELECT AVG(fcr) FROM fcr_calculations WHERE pondId = :pondId AND cycleEndDate IS NOT NULL")
    suspend fun getAverageFCR(pondId: Long): Double

    @Query("SELECT AVG(fcr) FROM fcr_calculations WHERE cycleEndDate IS NOT NULL")
    suspend fun getGlobalAverageFCR(): Double

    @Query("SELECT * FROM fcr_calculations WHERE pondId = :pondId ORDER BY cycleStartDate DESC LIMIT 1")
    suspend fun getLatestCalculation(pondId: Long): FCRCalculationEntity?
}
