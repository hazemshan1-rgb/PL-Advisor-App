package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity representing a batch of postlarval (PL) shrimp.
 */
@Entity(
    tableName = "pl_batches",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PLBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val hatcheryName: String,
    val batchNumber: String,
    val plSize: Int, // PL10, PL15, etc.
    val quantity: Int, // number of PLs
    val quantityStocked: Int, // actual stocked
    val qualityScore: Double = 0.0, // 0-100
    val isSPF: Boolean, // Specific Pathogen Free
    val certificationsUrl: String?, // path to documents
    val photoUrl: String?,
    val stockingDate: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val notes: String = ""
)
