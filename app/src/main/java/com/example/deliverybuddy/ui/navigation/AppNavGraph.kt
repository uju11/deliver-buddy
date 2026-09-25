package com.example.deliverybuddy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.deliverybuddy.ui.screens.HistoryScreen
import com.example.deliverybuddy.ui.screens.RoutePlanScreen
import com.example.deliverybuddy.ui.screens.RunsheetDetailScreen
import com.example.deliverybuddy.ui.screens.RunsheetListScreen
import com.example.deliverybuddy.ui.screens.SettingsScreen
import com.example.deliverybuddy.ui.viewmodel.DeliveryViewModel

@Composable
fun AppNavGraph(
    viewModel: DeliveryViewModel
) {
    val backStack = remember { mutableStateListOf<Any>(NavKeys.RunsheetList) }
    val runsheets by viewModel.runsheets.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val vehicleMileage by viewModel.vehicleMileage.collectAsStateWithLifecycle()
    val fuelPrice by viewModel.fuelPrice.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }
    ) { key ->
        when (key) {
            is NavKeys.RunsheetList -> NavEntry(key) {
                RunsheetListScreen(
                    runsheets = runsheets,
                    onRunsheetClick = { runsheetId ->
                        backStack.add(NavKeys.RunsheetDetail(runsheetId))
                    },
                    onSettingsClick = {
                        backStack.add(NavKeys.Settings)
                    },
                    onHistoryClick = {
                        backStack.add(NavKeys.History)
                    },
                    onParseAndAddRunsheet = { rawText ->
                        viewModel.parseAndAddRunsheets(rawText)
                    }
                )
            }

            is NavKeys.RunsheetDetail -> NavEntry(key) {
                val runsheet = viewModel.getRunsheet(key.runsheetId)
                RunsheetDetailScreen(
                    runsheet = runsheet,
                    vehicleMileage = vehicleMileage,
                    fuelPrice = fuelPrice,
                    onBackClick = { backStack.removeLastOrNull() },
                    onViewRoutePlan = { runsheetId ->
                        backStack.add(NavKeys.RoutePlan(runsheetId))
                    },
                    onToggleAddress = { runsheetId, addressId, currentStatus ->
                        viewModel.toggleAddressCompletion(runsheetId, addressId, currentStatus)
                    },
                    onSortRunsheet = { runsheetId, sortOrder ->
                        viewModel.sortAddresses(runsheetId, sortOrder)
                    },
                    onCompleteAndArchive = { runsheetId ->
                        viewModel.completeAndArchiveRunsheet(runsheetId)
                        backStack.removeLastOrNull()
                    }
                )
            }

            is NavKeys.RoutePlan -> NavEntry(key) {
                val routePlan = viewModel.getRoutePlan(key.runsheetId)
                val runsheet = viewModel.getRunsheet(key.runsheetId)
                val nearbyPumps = viewModel.getNearbyPetrolPumps(key.runsheetId)
                RoutePlanScreen(
                    routePlan = routePlan,
                    allAddresses = runsheet?.addresses.orEmpty(),
                    nearbyPetrolPumps = nearbyPumps,
                    vehicleMileage = vehicleMileage,
                    fuelPrice = fuelPrice,
                    onRecalculateRoute = { startId, endId ->
                        viewModel.getRoutePlan(key.runsheetId, startId, endId)
                    },
                    onAddPetrolStop = { pump, startId, endId ->
                        viewModel.addPetrolStopToRoutePlan(key.runsheetId, pump, startId, endId)
                    },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }

            is NavKeys.Settings -> NavEntry(key) {
                SettingsScreen(
                    currentMileage = vehicleMileage,
                    currentFuelPrice = fuelPrice,
                    onUpdateSettings = { mileage, price ->
                        viewModel.updateSettings(mileage, price)
                    },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }

            is NavKeys.History -> NavEntry(key) {
                HistoryScreen(
                    historyRecords = historyRecords,
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }

            else -> NavEntry(Unit) {
                // Fallback
            }
        }
    }
}
