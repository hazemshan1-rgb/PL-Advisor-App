package com.shrimpadvisor.plcycle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionProfileDao {

    @Query("SELECT * FROM region_profiles ORDER BY isBuiltIn DESC, regionName ASC")
    fun getAllProfiles(): Flow<List<RegionProfile>>

    @Query("SELECT * FROM region_profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): RegionProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: RegionProfile): Long

    @Update
    suspend fun update(profile: RegionProfile)

    @Delete
    suspend fun delete(profile: RegionProfile)
}
