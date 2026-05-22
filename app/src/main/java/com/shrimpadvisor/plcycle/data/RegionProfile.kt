package com.shrimpadvisor.plcycle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_profiles")
data class RegionProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val regionName: String,
    val currency: String = "USD",
    // Price per kg at each market size bracket
    val priceAt20g: Double = 3.5,
    val priceAt25g: Double = 4.0,
    val priceAt30g: Double = 5.0,
    val priceAt35g: Double = 6.0,
    val priceAt40g: Double = 7.0,
    // Default input costs
    val feedCostDefault: Double = 1.25,
    val laborCostDefault: Double = 4.0,
    val aerationCostDefault: Double = 8.0,
    val probioticCostDefault: Double = 3.0,
    val isBuiltIn: Boolean = false  // built-in regions can't be deleted
)
