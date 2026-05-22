package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Daily mortality log entry for a pond.
 */
@Entity(
    tableName = "mortality_logs",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MortalityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val date: LocalDateTime,
    val mortalityCount: Int, // number of dead shrimp
    val symptoms: String, // comma-separated: red gills, white spots, lethargy, etc.
    val waterQualityNotes: String = "",
    val recordedBy: String = "",
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val syncedAt: LocalDateTime? = null // for offline sync tracking
)
