package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Daily water quality monitoring for a pond.
 */
@Entity(
    tableName = "water_quality_logs",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WaterQualityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val date: LocalDateTime,
    val temperature: Double, // Celsius
    val ph: Double, // 0-14
    val dissolvedOxygen: Double, // mg/L
    val ammonia: Double, // mg/L NH3
    val salinity: Double, // ppt (parts per thousand)
    val nitrite: Double? = null, // mg/L
    val alkalinity: Double? = null, // mg/L CaCO3
    val hardness: Double? = null, // mg/L CaCO3
    val recordedBy: String = "",
    val sensorId: String? = null, // for future IoT integration
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val syncedAt: LocalDateTime? = null
)
