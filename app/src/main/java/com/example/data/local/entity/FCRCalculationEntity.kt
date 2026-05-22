package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Calculated FCR (Feed Conversion Ratio) record.
 * FCR = Total Feed Given (kg) / Biomass Gained (kg)
 */
@Entity(
    tableName = "fcr_calculations",
    foreignKeys = [
        ForeignKey(
            entity = PondEntity::class,
            parentColumns = ["id"],
            childColumns = ["pondId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FCRCalculationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pondId: Long,
    val cycleStartDate: LocalDateTime,
    val cycleEndDate: LocalDateTime? = null,
    val totalFeedKg: Double,
    val initialBiomasKg: Double,
    val finalBiomasKg: Double,
    val biomasGainedKg: Double, // final - initial
    val fcr: Double, // calculated: totalFeed / gainedBiomass
    val notes: String = "",
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)
