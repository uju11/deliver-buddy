package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
data class FuelAnalytics(
    val totalKmRun: Double,
    val dailyMileageKm: Double,
    val projectedMonthlyFuelCost: Double,
    val actualMonthlyFuelCost: Double,
    val dailyFuelCost: Double
)
