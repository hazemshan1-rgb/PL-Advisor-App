package com.shrimpadvisor.plcycle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pond_cycles")
data class PondCycle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pondName: String = "Pond #1",
    val stockingDate: Long = System.currentTimeMillis(),
    
    // Physical & Capacity Specs
    val pondSize: Double = 1000.0, // m²
    val proposedDensity: Double = 60.0, // PL/m²
    val harvestWeightTarget: Double = 20.0, // g
    val marketPriceTarget: Double = 8.0, // USD/kg
    
    // Pre-stocking Quality Scores (0 - 100)
    val stressToleranceScore: Int = 85,
    val gutFullnessScore: Int = 80,
    val supplierScore: Int = 90,
    
    // Water Quality Parameters
    val temp: Double = 28.5, // °C
    val ph: Double = 8.0,
    val salinity: Double = 25.0, // ppt
    val doLevel: Double = 6.0, // ppm / mg/L
    val tanLevel: Double = 0.2, // ppm
    
    // Continuous Monitoring State
    val estimatedSurvival: Double = 80.0, // %
    val currentAbw: Double = 5.0, // Average Body Weight in g
    val currentAge: Int = 15, // Days from stocking
    val totalFeedConsumed: Double = 120.0, // kg
    val averageDailyGain: Double = 0.23, // g/day
    
    // Cost Tracking Parameters
    val feedCostPerKg: Double = 1.25, // USD/kg
    val plUnitCost: Double = 0.012, // USD/PL (e.g. 1.2 cents)
    val aerationCostPerDay: Double = 8.0, // USD/day
    val probioticCostPerDay: Double = 3.0, // USD/day
    val laborCostPerDay: Double = 4.0, // USD/day

    // Mortality Configuration
    val customMortalityRate: Double = 0.004, // fraction per day (0.004 = 0.4%/day)
    val mortalityAcceleration: Double = 0.0,  // added fraction per day each week (0.0005 = +0.05%/day per week)

    // Regional Profile Link
    val regionProfileId: Int? = null,

    // Configurable Agronomic Coefficients
    val carryingCapacityRatio: Double = 6.0, // kg/m²
    val diseaseMortalityMultiplier: Double = 2.5,
    val week1SurvivalBaselineStock: Double = 85.0,
    val week1SurvivalBaselineHold: Double = 75.0,
    val week1SurvivalBaselineReject: Double = 50.0
)
