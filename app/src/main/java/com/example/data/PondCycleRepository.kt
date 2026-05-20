package com.example.data

import kotlinx.coroutines.flow.Flow

class PondCycleRepository(private val pondCycleDao: PondCycleDao) {
    
    val allCycles: Flow<List<PondCycle>> = pondCycleDao.getAllCycles()

    fun getCycleById(id: Int): Flow<PondCycle?> {
        return pondCycleDao.getCycleById(id)
    }

    suspend fun insert(cycle: PondCycle): Long {
        return pondCycleDao.insertCycle(cycle)
    }

    suspend fun update(cycle: PondCycle) {
        pondCycleDao.updateCycle(cycle)
    }

    suspend fun delete(cycle: PondCycle) {
        pondCycleDao.deleteCycle(cycle)
    }
}
