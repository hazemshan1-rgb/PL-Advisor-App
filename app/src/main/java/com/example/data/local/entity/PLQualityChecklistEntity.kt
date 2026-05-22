package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Quality assessment checklist for PL batch.
 */
@Entity(
    tableName = "pl_quality_checklists",
    foreignKeys = [
        ForeignKey(
            entity = PLBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PLQualityChecklistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchId: Long,
    val uniformSize: Boolean,
    val activeSwimming: Boolean,
    val erectRostrum: Boolean,
    val clearBody: Boolean,
    val noVisibleDiseases: Boolean,
    val stressTestSurvivalPercent: Double, // should be > 90%
    val overallQualityScore: Double, // 0-100
    val checkedAt: LocalDateTime = LocalDateTime.now(),
    val checkedBy: String = "",
    val notes: String = ""
)
