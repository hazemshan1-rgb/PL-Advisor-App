package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PLQualityChecklistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PLQualityChecklistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: PLQualityChecklistEntity): Long

    @Update
    suspend fun updateChecklist(checklist: PLQualityChecklistEntity)

    @Query("SELECT * FROM pl_quality_checklists WHERE batchId = :batchId")
    suspend fun getChecklistsByBatch(batchId: Long): List<PLQualityChecklistEntity>

    @Query("SELECT * FROM pl_quality_checklists WHERE batchId = :batchId ORDER BY checkedAt DESC LIMIT 1")
    suspend fun getLatestChecklistByBatch(batchId: Long): PLQualityChecklistEntity?

    @Query("SELECT AVG(overallQualityScore) FROM pl_quality_checklists WHERE batchId = :batchId")
    suspend fun getAverageQualityScore(batchId: Long): Double

    @Query("SELECT * FROM pl_quality_checklists WHERE checkedAt >= datetime('now', '-7 days') ORDER BY checkedAt DESC")
    fun getChecklistsFromLastWeek(): Flow<List<PLQualityChecklistEntity>>
}
