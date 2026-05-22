package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Stocking density calculation and recommendation record.
 */
@Entity(
    tableName = "stocking_densities",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PLBatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StockingDensityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val batchId: Long,
    val densityPerM2: Double, // calculated: stocked quantity / pond size
    val recommendedDensityMin: Double, // min for system type
    val recommendedDensityMax: Double, // max for system type
    val riskLevel: String, // "optimal", "high", "critical"
    val oxygenCapacityIndex: Double?, // 0-1, capacity for aeration
    val bioloadCapacityIndex: Double?, // 0-1, capacity for waste management
    val recommendations: String = "",
    val calculatedAt: LocalDateTime = LocalDateTime.now()
)
