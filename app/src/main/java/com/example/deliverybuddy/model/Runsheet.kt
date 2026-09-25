package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
enum class RunsheetStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

@Serializable
data class Runsheet(
    val id: String,
    val title: String,
    val date: String,
    val driverName: String,
    val status: RunsheetStatus = RunsheetStatus.PENDING,
    val addresses: List<Address> = emptyList(),
    val totalDistanceKm: Double = 0.0,
    val estimatedDurationMinutes: Int = 0
)
