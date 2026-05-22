package com.shrimpadvisor.plcycle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PondCycleDao {
    @Query("SELECT * FROM pond_cycles ORDER BY stockingDate DESC")
    fun getAllCycles(): Flow<List<PondCycle>>

    @Query("SELECT * FROM pond_cycles WHERE id = :id LIMIT 1")
    fun getCycleById(id: Int): Flow<PondCycle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: PondCycle): Long

    @Update
    suspend fun updateCycle(cycle: PondCycle)

    @Delete
    suspend fun deleteCycle(cycle: PondCycle)
}
