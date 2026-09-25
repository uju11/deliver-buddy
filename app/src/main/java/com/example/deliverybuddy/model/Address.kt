package com.example.deliverybuddy.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val contactName: String,
    val phoneNumber: String,
    val deliveryNotes: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isCompleted: Boolean = false,
    val priority: Int = 1
)
