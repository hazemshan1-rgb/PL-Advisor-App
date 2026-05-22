package com.shrimpadvisor.plcycle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReadingDao {

    @Query("SELECT * FROM daily_readings WHERE pondCycleId = :cycleId ORDER BY timestamp ASC")
    fun getReadingsForCycle(cycleId: Int): Flow<List<DailyReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: DailyReading): Long

    @Delete
    suspend fun delete(reading: DailyReading)

    @Query("DELETE FROM daily_readings WHERE pondCycleId = :cycleId")
    suspend fun deleteAllForCycle(cycleId: Int)
}
