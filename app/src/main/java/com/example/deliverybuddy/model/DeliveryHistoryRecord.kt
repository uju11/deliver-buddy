package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryHistoryRecord(
    val id: String,
    val runsheetId: String,
    val title: String,
    val timestamp: String,
    val date: String,
    val driverName: String,
    val totalStops: Int,
    val completedStops: Int,
    val totalDistanceKm: Double,
    val actualDistanceKm: Double,
    val projectedFuelCost: Double,
    val actualFuelCost: Double
)
