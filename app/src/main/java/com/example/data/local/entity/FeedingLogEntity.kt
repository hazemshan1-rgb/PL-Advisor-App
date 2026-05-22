package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Daily feed input log for FCR calculation.
 */
@Entity(
    tableName = "feeding_logs",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FeedingLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val date: LocalDateTime,
    val feedTypeId: Long, // reference to feed type/brand
    val feedQuantityKg: Double,
    val feedCostPerKg: Double,
    val time: String? = null, // time of feeding
    val feedingObservation: String = "", // e.g., "good appetite", "slow consumption"
    val recordedBy: String = "",
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val syncedAt: LocalDateTime? = null
)
