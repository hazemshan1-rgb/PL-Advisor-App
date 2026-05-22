package com.example.data.repository

import com.example.data.local.dao.PondDao
import com.example.data.local.entity.PondEntity
import com.example.domain.repository.PondRepository
import kotlinx.coroutines.flow.Flow

class PondRepositoryImpl(private val pondDao: PondDao) : PondRepository {
    override suspend fun addPond(pond: PondEntity): Long = pondDao.insertPond(pond)

    override suspend fun updatePond(pond: PondEntity) = pondDao.updatePond(pond)

    override suspend fun deletePond(pond: PondEntity) = pondDao.deletePond(pond)

    override suspend fun getPondById(pondId: Long): PondEntity? = pondDao.getPondById(pondId)

    override fun getPondsByFarm(farmId: Long): Flow<List<PondEntity>> = pondDao.getPondsByFarm(farmId)

    override suspend fun getPondsByFarmOnce(farmId: Long): List<PondEntity> = pondDao.getPondsByFarmOnce(farmId)

    override fun getAllActivePonds(): Flow<List<PondEntity>> = pondDao.getAllActivePonds()

    override suspend fun deactivatePond(pondId: Long) = pondDao.deactivatePond(pondId)
}
