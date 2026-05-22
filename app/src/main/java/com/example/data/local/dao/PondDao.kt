package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PondEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PondDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPond(pond: PondEntity): Long

    @Update
    suspend fun updatePond(pond: PondEntity)

    @Delete
    suspend fun deletePond(pond: PondEntity)

    @Query("SELECT * FROM ponds WHERE id = :pondId")
    suspend fun getPondById(pondId: Long): PondEntity?

    @Query("SELECT * FROM ponds WHERE farmId = :farmId AND isActive = 1 ORDER BY name")
    fun getPondsByFarm(farmId: Long): Flow<List<PondEntity>>

    @Query("SELECT * FROM ponds WHERE farmId = :farmId")
    suspend fun getPondsByFarmOnce(farmId: Long): List<PondEntity>

    @Query("SELECT * FROM ponds WHERE isActive = 1")
    fun getAllActivePonds(): Flow<List<PondEntity>>

    @Query("UPDATE ponds SET isActive = 0 WHERE id = :pondId")
    suspend fun deactivatePond(pondId: Long)
}
