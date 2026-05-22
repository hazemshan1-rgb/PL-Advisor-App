package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Biomass estimation from size sampling (for FCR calculation).
 */
@Entity(
    tableName = "biomass_estimates",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BiomassEstimateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val samplingDate: LocalDateTime,
    val numberOfSamples: Int, // number of shrimp sampled
    val averageSizeG: Double, // average weight in grams
    val estimatedCount: Int, // estimated population
    val estimatedBiomasKg: Double, // total biomass in kg
    val sampledBy: String = "",
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
