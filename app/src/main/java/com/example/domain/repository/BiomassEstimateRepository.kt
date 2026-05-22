package com.example.domain.repository

import com.example.data.local.entity.BiomassEstimateEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface BiomassEstimateRepository {
    suspend fun addEstimate(estimate: BiomassEstimateEntity): Long
    suspend fun updateEstimate(estimate: BiomassEstimateEntity)
    suspend fun deleteEstimate(estimate: BiomassEstimateEntity)
    fun getEstimatesByPond(pondId: Long): Flow<List<BiomassEstimateEntity>>
    fun getEstimatesByPondInDateRange(pondId: Long, startDate: LocalDateTime): Flow<List<BiomassEstimateEntity>>
    suspend fun getLatestEstimateByPond(pondId: Long): BiomassEstimateEntity?
    suspend fun getAverageBiomass(pondId: Long, startDate: LocalDateTime): Double
}
