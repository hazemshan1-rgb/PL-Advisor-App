package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Weekly cast net sampling for survival rate calculation.
 */
@Entity(
    tableName = "survivorship_samplings",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SurvivorshipSamplingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val samplingDate: LocalDateTime,
    val numberOfCasts: Int, // number of cast net casts
    val shrimpCaught: Int, // total caught in sampling
    val estimatedPopulation: Int, // extrapolated estimate
    val averageSizeG: Double, // average weight in grams
    val sampledBy: String = "",
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
