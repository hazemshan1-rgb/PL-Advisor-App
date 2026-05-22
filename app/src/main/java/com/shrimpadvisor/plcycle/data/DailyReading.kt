package com.shrimpadvisor.plcycle.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_readings",
    foreignKeys = [
        ForeignKey(
            entity = PondCycle::class,
            parentColumns = ["id"],
            childColumns = ["pondCycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pondCycleId")]
)
data class DailyReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pondCycleId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val pondAge: Int,
    val survivalPct: Double,
    val doLevel: Double,
    val tanLevel: Double,
    val ph: Double,
    val temp: Double,
    val abw: Double,
    val notes: String = ""
)
