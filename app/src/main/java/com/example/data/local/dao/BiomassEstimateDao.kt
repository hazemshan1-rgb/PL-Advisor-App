package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.BiomassEstimateEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface BiomassEstimateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimate(estimate: BiomassEstimateEntity): Long

    @Update
    suspend fun updateEstimate(estimate: BiomassEstimateEntity)

    @Delete
    suspend fun deleteEstimate(estimate: BiomassEstimateEntity)

    @Query("SELECT * FROM biomass_estimates WHERE pondId = :pondId ORDER BY samplingDate DESC")
    fun getEstimatesByPond(pondId: Long): Flow<List<BiomassEstimateEntity>>

    @Query("SELECT * FROM biomass_estimates WHERE pondId = :pondId AND samplingDate >= :startDate ORDER BY samplingDate DESC")
    fun getEstimatesByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<BiomassEstimateEntity>>

    @Query("SELECT * FROM biomass_estimates WHERE pondId = :pondId ORDER BY samplingDate DESC LIMIT 1")
    suspend fun getLatestEstimateByPond(pondId: Long): BiomassEstimateEntity?

    @Query("SELECT AVG(estimatedBiomasKg) FROM biomass_estimates WHERE pondId = :pondId AND samplingDate >= :startDate")
    suspend fun getAverageBiomass(pondId: Long, startDate: LocalDateTime): Double
}
