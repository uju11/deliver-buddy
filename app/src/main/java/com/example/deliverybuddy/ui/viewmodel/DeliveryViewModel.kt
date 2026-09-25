package com.example.deliverybuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.deliverybuddy.data.DeliveryRepository
import com.example.deliverybuddy.data.RunsheetParser
import com.example.deliverybuddy.data.SortOrder
import com.example.deliverybuddy.model.DeliveryHistoryRecord
import com.example.deliverybuddy.model.FuelAnalytics
import com.example.deliverybuddy.model.PetrolPump
import com.example.deliverybuddy.model.RoutePlan
import com.example.deliverybuddy.model.Runsheet
import kotlinx.coroutines.flow.StateFlow

class DeliveryViewModel(
    private val repository: DeliveryRepository = DeliveryRepository()
) : ViewModel() {

    val runsheets: StateFlow<List<Runsheet>> = repository.runsheets
    val historyRecords: StateFlow<List<DeliveryHistoryRecord>> = repository.historyRecords
    val vehicleMileage: StateFlow<Float> = repository.vehicleMileage
    val fuelPrice: StateFlow<Float> = repository.fuelPrice
    val currentLocation: StateFlow<Pair<Double, Double>?> = repository.currentLocation

    fun updateSettings(mileage: Float, fuelPrice: Float) {
        repository.updateSettings(mileage, fuelPrice)
    }

    fun updateCurrentLocation(lat: Double, lon: Double) {
        repository.updateCurrentLocation(lat, lon)
    }

    fun getFuelAnalytics(): FuelAnalytics {
        return repository.getFuelAnalytics()
    }

    fun getRunsheet(id: String): Runsheet? {
        return repository.getRunsheetById(id)
    }

    fun toggleAddressCompletion(runsheetId: String, addressId: String, currentStatus: Boolean) {
        repository.updateAddressStatus(runsheetId, addressId, !currentStatus)
    }

    fun completeAndArchiveRunsheet(runsheetId: String) {
        repository.completeAndArchiveRunsheet(runsheetId)
    }

    fun sortAddresses(runsheetId: String, sortOrder: SortOrder) {
        repository.sortRunsheetAddresses(runsheetId, sortOrder)
    }

    fun parseAndAddRunsheets(rawText: String) {
        val parsed = RunsheetParser.parseRunsheets(rawText)
        if (parsed.isNotEmpty()) {
            repository.addRunsheets(parsed)
        }
    }

    fun getRoutePlan(
        runsheetId: String,
        startAddressId: String? = null,
        endAddressId: String? = null
    ): RoutePlan? {
        return repository.generateRoutePlan(runsheetId, startAddressId, endAddressId)
    }

    fun getNearbyPetrolPumps(runsheetId: String): List<PetrolPump> {
        return repository.getNearbyPetrolPumps(runsheetId)
    }

    fun addPetrolStopToRoutePlan(
        runsheetId: String,
        petrolPump: PetrolPump,
        startAddressId: String? = null,
        endAddressId: String? = null
    ): RoutePlan? {
        return repository.addPetrolStopToRoutePlan(runsheetId, petrolPump, startAddressId, endAddressId)
    }
}
