package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
data class PetrolPump(
    val id: String,
    val name: String,
    val street: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val pricePerLiter: Double = 1.50
)
