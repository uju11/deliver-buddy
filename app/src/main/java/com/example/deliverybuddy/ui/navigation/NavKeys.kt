package com.example.deliverybuddy.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavKeys {
    @Serializable
    data object RunsheetList : NavKeys

    @Serializable
    data class RunsheetDetail(val runsheetId: String) : NavKeys

    @Serializable
    data class RoutePlan(val runsheetId: String) : NavKeys

    @Serializable
    data object Settings : NavKeys

    @Serializable
    data object History : NavKeys
}
