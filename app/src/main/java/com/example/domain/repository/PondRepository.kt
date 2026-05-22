package com.example.domain.repository

import com.example.data.local.entity.PondEntity
import kotlinx.coroutines.flow.Flow

interface PondRepository {
    suspend fun addPond(pond: PondEntity): Long
    suspend fun updatePond(pond: PondEntity)
    suspend fun deletePond(pond: PondEntity)
    suspend fun getPondById(pondId: Long): PondEntity?
    fun getPondsByFarm(farmId: Long): Flow<List<PondEntity>>
    suspend fun getPondsByFarmOnce(farmId: Long): List<PondEntity>
    fun getAllActivePonds(): Flow<List<PondEntity>>
    suspend fun deactivatePond(pondId: Long)
}
