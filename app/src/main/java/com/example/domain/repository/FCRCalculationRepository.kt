package com.example.domain.repository

import com.example.data.local.entity.FCRCalculationEntity
import kotlinx.coroutines.flow.Flow

interface FCRCalculationRepository {
    suspend fun addCalculation(calculation: FCRCalculationEntity): Long
    suspend fun updateCalculation(calculation: FCRCalculationEntity)
    suspend fun deleteCalculation(calculation: FCRCalculationEntity)
    fun getCalculationsByPond(pondId: Long): Flow<List<FCRCalculationEntity>>
    suspend fun getActiveCycleByPond(pondId: Long): FCRCalculationEntity?
    suspend fun getAverageFCR(pondId: Long): Double
    suspend fun getGlobalAverageFCR(): Double
    suspend fun getLatestCalculation(pondId: Long): FCRCalculationEntity?
}
