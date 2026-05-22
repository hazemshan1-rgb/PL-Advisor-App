package com.shrimpadvisor.plcycle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReadingDao {

    @Query("SELECT * FROM daily_readings WHERE pondCycleId = :cycleId ORDER BY timestamp ASC")
    fun getReadingsForCycle(cycleId: Int): Flow<List<DailyReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: DailyReading): Long

    @Update
    suspend fun update(reading: DailyReading)

    @Delete
    suspend fun delete(reading: DailyReading)

    @Query("DELETE FROM daily_readings WHERE pondCycleId = :cycleId")
    suspend fun deleteAllForCycle(cycleId: Int)

    @Query("SELECT * FROM daily_readings WHERE pondCycleId = :cycleId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReadingsForCycle(cycleId: Int, limit: Int): Flow<List<DailyReading>>

    @Query("SELECT SUM(feedGiven) FROM daily_readings WHERE pondCycleId = :cycleId")
    fun getTotalFeedGiven(cycleId: Int): Flow<Double?>
}
