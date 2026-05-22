package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
from java.time.LocalDateTime

/**
 * Entity representing a pond or tank in the farm.
 */
@Entity(tableName = "ponds")
data class PondEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // extensive, semi-intensive, intensive
    val sizeM2: Double, // in square meters
    val maxDepthM: Double,
    val aeration: String, // none, low, medium, high
    val farmId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)
