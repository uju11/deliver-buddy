package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
data class RoutePlan(
    val id: String,
    val runsheetId: String,
    val optimizedStops: List<Address>,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val polylinePoints: List<Pair<Double, Double>> = emptyList()
)
