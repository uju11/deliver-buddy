package com.example.deliverybuddy.data

import com.example.deliverybuddy.model.Address
import com.example.deliverybuddy.model.DeliveryHistoryRecord
import com.example.deliverybuddy.model.PetrolPump
import com.example.deliverybuddy.model.RoutePlan
import com.example.deliverybuddy.model.Runsheet
import com.example.deliverybuddy.model.RunsheetStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SortOrder {
    DEFAULT,
    CLOSEST_FIRST,
    FURTHEST_FIRST,
    PRIORITY,
    STATUS
}

class DeliveryRepository {
    companion object {
        private val sampleAddresses1 = listOf(
            Address(
                id = "addr_1",
                street = "123 Market St",
                city = "San Francisco, CA",
                postalCode = "94105",
                contactName = "Alice Johnson",
                phoneNumber = "415-555-0192",
                deliveryNotes = "Leave at front desk",
                latitude = 37.7890,
                longitude = -122.3970,
                isCompleted = true,
                priority = 1
            ),
            Address(
                id = "addr_2",
                street = "456 Mission St",
                city = "San Francisco, CA",
                postalCode = "94105",
                contactName = "Bob Smith",
                phoneNumber = "415-555-0143",
                deliveryNotes = "Call upon arrival",
                latitude = 37.7850,
                longitude = -122.3990,
                isCompleted = false,
                priority = 2
            ),
            Address(
                id = "addr_3",
                street = "789 Howard St",
                city = "San Francisco, CA",
                postalCode = "94103",
                contactName = "Charlie Davis",
                phoneNumber = "415-555-0188",
                deliveryNotes = "Side entrance",
                latitude = 37.7820,
                longitude = -122.4010,
                isCompleted = false,
                priority = 1
            )
        )

        private val sampleAddresses2 = listOf(
            Address(
                id = "addr_4",
                street = "1000 Broadway",
                city = "Oakland, CA",
                postalCode = "94607",
                contactName = "Diana Prince",
                phoneNumber = "510-555-0122",
                deliveryNotes = "Ring doorbell twice",
                latitude = 37.8044,
                longitude = -122.2711,
                isCompleted = false,
                priority = 1
            ),
            Address(
                id = "addr_5",
                street = "200 Grand Ave",
                city = "Oakland, CA",
                postalCode = "94610",
                contactName = "Bruce Wayne",
                phoneNumber = "510-555-0199",
                deliveryNotes = "Secure delivery zone",
                latitude = 37.8100,
                longitude = -122.2580,
                isCompleted = false,
                priority = 2
            )
        )

        private val initialRunsheets = listOf(
            Runsheet(
                id = "run_1",
                title = "Downtown Express Dispatch",
                date = "2025-05-20",
                driverName = "John Driver",
                status = RunsheetStatus.IN_PROGRESS,
                addresses = sampleAddresses1,
                totalDistanceKm = 12.5,
                estimatedDurationMinutes = 45
            ),
            Runsheet(
                id = "run_2",
                title = "East Bay Bulk Delivery",
                date = "2025-05-20",
                driverName = "John Driver",
                status = RunsheetStatus.PENDING,
                addresses = sampleAddresses2,
                totalDistanceKm = 24.0,
                estimatedDurationMinutes = 90
            )
        )

        private val initialHistory = listOf(
            DeliveryHistoryRecord(
                id = "hist_1",
                runsheetId = "run_archived_1",
                title = "Historic Morning Dispatch",
                timestamp = "2025-05-19 14:30",
                date = "2025-05-19",
                driverName = "John Driver",
                totalStops = 4,
                completedStops = 4,
                totalDistanceKm = 18.0,
                actualDistanceKm = 17.5,
                projectedFuelCost = 2.25,
                actualFuelCost = 2.18
            )
        )

        private val _runsheets = MutableStateFlow(initialRunsheets)
        private val _historyRecords = MutableStateFlow(initialHistory)
        private val _vehicleMileage = MutableStateFlow(12.0f)
        private val _fuelPrice = MutableStateFlow(1.50f)
    }

    val runsheets: StateFlow<List<Runsheet>> = _runsheets.asStateFlow()
    val historyRecords: StateFlow<List<DeliveryHistoryRecord>> = _historyRecords.asStateFlow()
    val vehicleMileage: StateFlow<Float> = _vehicleMileage.asStateFlow()
    val fuelPrice: StateFlow<Float> = _fuelPrice.asStateFlow()

    fun updateSettings(mileage: Float, fuelPrice: Float) {
        if (mileage > 0f) _vehicleMileage.value = mileage
        if (fuelPrice >= 0f) _fuelPrice.value = fuelPrice
    }

    fun getRunsheetById(id: String): Runsheet? {
        return _runsheets.value.find { it.id == id }
    }

    fun addRunsheets(newRunsheets: List<Runsheet>) {
        _runsheets.update { current ->
            newRunsheets + current
        }
    }

    fun updateAddressStatus(runsheetId: String, addressId: String, isCompleted: Boolean) {
        _runsheets.update { currentList ->
            currentList.map { runsheet ->
                if (runsheet.id == runsheetId) {
                    val updatedAddresses = runsheet.addresses.map { address ->
                        if (address.id == addressId) {
                            address.copy(isCompleted = isCompleted)
                        } else {
                            address
                        }
                    }
                    val allCompleted = updatedAddresses.all { it.isCompleted }
                    val anyCompleted = updatedAddresses.any { it.isCompleted }
                    val newStatus = when {
                        allCompleted -> RunsheetStatus.COMPLETED
                        anyCompleted -> RunsheetStatus.IN_PROGRESS
                        else -> RunsheetStatus.PENDING
                    }
                    runsheet.copy(addresses = updatedAddresses, status = newStatus)
                } else {
                    runsheet
                }
            }
        }
    }

    fun completeAndArchiveRunsheet(runsheetId: String) {
        val runsheet = getRunsheetById(runsheetId) ?: return
        val totalStops = runsheet.addresses.size
        val completedStops = runsheet.addresses.count { it.isCompleted }
        val projectedDist = runsheet.totalDistanceKm
        val actualDist = if (totalStops > 0) (completedStops.toFloat() / totalStops) * projectedDist else projectedDist

        val mileage = _vehicleMileage.value
        val price = _fuelPrice.value

        val projectedCost = if (mileage > 0f) (projectedDist / mileage) * price else 0.0
        val actualCost = if (mileage > 0f) (actualDist / mileage) * price else 0.0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val historyRecord = DeliveryHistoryRecord(
            id = "hist_${System.currentTimeMillis()}",
            runsheetId = runsheet.id,
            title = runsheet.title,
            timestamp = timestamp,
            date = runsheet.date,
            driverName = runsheet.driverName,
            totalStops = totalStops,
            completedStops = completedStops,
            totalDistanceKm = projectedDist,
            actualDistanceKm = String.format(Locale.getDefault(), "%.1f", actualDist).toDouble(),
            projectedFuelCost = String.format(Locale.getDefault(), "%.2f", projectedCost).toDouble(),
            actualFuelCost = String.format(Locale.getDefault(), "%.2f", actualCost).toDouble()
        )

        _historyRecords.update { listOf(historyRecord) + it }

        // Mark runsheet completed and all addresses completed
        _runsheets.update { currentList ->
            currentList.map { r ->
                if (r.id == runsheetId) {
                    r.copy(
                        status = RunsheetStatus.COMPLETED,
                        addresses = r.addresses.map { it.copy(isCompleted = true) }
                    )
                } else {
                    r
                }
            }
        }
    }

    fun sortRunsheetAddresses(runsheetId: String, sortOrder: SortOrder) {
        _runsheets.update { currentList ->
            currentList.map { runsheet ->
                if (runsheet.id == runsheetId) {
                    val refLat = runsheet.addresses.firstOrNull()?.latitude ?: 37.7749
                    val refLon = runsheet.addresses.firstOrNull()?.longitude ?: -122.4194

                    val sortedAddresses = when (sortOrder) {
                        SortOrder.DEFAULT -> runsheet.addresses.sortedBy { it.priority }
                        SortOrder.CLOSEST_FIRST -> runsheet.addresses.sortedBy { addr ->
                            calculateDistance(refLat, refLon, addr.latitude, addr.longitude)
                        }
                        SortOrder.FURTHEST_FIRST -> runsheet.addresses.sortedByDescending { addr ->
                            calculateDistance(refLat, refLon, addr.latitude, addr.longitude)
                        }
                        SortOrder.PRIORITY -> runsheet.addresses.sortedBy { it.priority }
                        SortOrder.STATUS -> runsheet.addresses.sortedWith(compareBy<Address> { it.isCompleted }.thenBy { it.priority })
                    }
                    runsheet.copy(addresses = sortedAddresses)
                } else {
                    runsheet
                }
            }
        }
    }

    fun generateRoutePlan(
        runsheetId: String,
        startAddressId: String? = null,
        endAddressId: String? = null
    ): RoutePlan? {
        val runsheet = getRunsheetById(runsheetId) ?: return null
        val originalAddresses = runsheet.addresses
        if (originalAddresses.isEmpty()) return null

        val startAddr = originalAddresses.find { it.id == startAddressId } ?: originalAddresses.first()
        val endAddr = originalAddresses.find { it.id == endAddressId && it.id != startAddr.id }

        val remaining = originalAddresses.filter { it.id != startAddr.id && it.id != endAddr?.id }

        // Optimize remaining stops using Nearest Neighbor TSP heuristic starting from startAddr
        val optimizedStops = mutableListOf<Address>()
        optimizedStops.add(startAddr)

        var current = startAddr
        val unvisited = remaining.toMutableList()
        while (unvisited.isNotEmpty()) {
            val next = unvisited.minByOrNull { addr ->
                calculateDistance(current.latitude, current.longitude, addr.latitude, addr.longitude)
            }!!
            unvisited.remove(next)
            optimizedStops.add(next)
            current = next
        }

        if (endAddr != null) {
            optimizedStops.add(endAddr)
        }

        // Calculate total distance in km
        var totalDist = 0.0
        for (i in 0 until optimizedStops.size - 1) {
            val a = optimizedStops[i]
            val b = optimizedStops[i + 1]
            totalDist += calculateDistance(a.latitude, a.longitude, b.latitude, b.longitude)
        }

        val durationMinutes = (totalDist * 3.5).toInt() + optimizedStops.size * 5

        return RoutePlan(
            id = "route_${runsheet.id}",
            runsheetId = runsheet.id,
            optimizedStops = optimizedStops,
            totalDistanceKm = String.format(Locale.getDefault(), "%.1f", totalDist).toDouble(),
            estimatedDurationMinutes = durationMinutes,
            polylinePoints = optimizedStops.map { Pair(it.latitude, it.longitude) }
        )
    }

    fun getNearbyPetrolPumps(runsheetId: String): List<PetrolPump> {
        val runsheet = getRunsheetById(runsheetId) ?: return emptyList()
        val firstAddr = runsheet.addresses.firstOrNull() ?: return emptyList()

        return listOf(
            PetrolPump(
                id = "pump_1",
                name = "Shell Express & Fuel",
                street = "780 3rd St",
                city = firstAddr.city,
                latitude = firstAddr.latitude + 0.005,
                longitude = firstAddr.longitude + 0.004,
                distanceKm = 1.2,
                pricePerLiter = _fuelPrice.value.toDouble()
            ),
            PetrolPump(
                id = "pump_2",
                name = "Chevron Service Center",
                street = "1200 Harrison St",
                city = firstAddr.city,
                latitude = firstAddr.latitude - 0.004,
                longitude = firstAddr.longitude + 0.006,
                distanceKm = 2.4,
                pricePerLiter = _fuelPrice.value * 0.98
            )
        )
    }

    fun addPetrolStopToRoutePlan(
        runsheetId: String,
        petrolPump: PetrolPump,
        startAddressId: String? = null,
        endAddressId: String? = null
    ): RoutePlan? {
        val basePlan = generateRoutePlan(runsheetId, startAddressId, endAddressId) ?: return null
        val stops = basePlan.optimizedStops.toMutableList()

        val pumpAddress = Address(
            id = petrolPump.id,
            street = petrolPump.name + " (" + petrolPump.street + ")",
            city = petrolPump.city,
            postalCode = "94107",
            contactName = "Fuel Station",
            phoneNumber = "800-555-FUEL",
            deliveryNotes = "Fill Petrol Stop - $${petrolPump.pricePerLiter}/L",
            latitude = petrolPump.latitude,
            longitude = petrolPump.longitude,
            isCompleted = false,
            priority = 0
        )

        val insertIndex = if (stops.size > 1) 1 else stops.size
        stops.add(insertIndex, pumpAddress)

        var totalDist = 0.0
        for (i in 0 until stops.size - 1) {
            val a = stops[i]
            val b = stops[i + 1]
            totalDist += calculateDistance(a.latitude, a.longitude, b.latitude, b.longitude)
        }

        val durationMinutes = (totalDist * 3.5).toInt() + stops.size * 5 + 10

        return basePlan.copy(
            id = "route_${runsheetId}_pump_${System.currentTimeMillis()}",
            optimizedStops = stops,
            totalDistanceKm = String.format(Locale.getDefault(), "%.1f", totalDist).toDouble(),
            estimatedDurationMinutes = durationMinutes,
            polylinePoints = stops.map { Pair(it.latitude, it.longitude) }
        )
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return 6371.0 * c
    }
}
