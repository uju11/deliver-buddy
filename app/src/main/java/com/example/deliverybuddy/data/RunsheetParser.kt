package com.example.deliverybuddy.data

import com.example.deliverybuddy.model.Address
import com.example.deliverybuddy.model.Runsheet
import com.example.deliverybuddy.model.RunsheetStatus

object RunsheetParser {
    /**
     * Parses raw text containing one or multiple runsheets.
     * Supports blocks separated by "---" or "RUNSHEET:".
     * Address lines can be pipe ('|'), semicolon, or comma separated:
     * Street | City | PostalCode | ContactName | PhoneNumber | DeliveryNotes
     */
    fun parseRunsheets(rawText: String): List<Runsheet> {
        if (rawText.isBlank()) return emptyList()

        val runsheetBlocks = rawText.split(Regex("(?i)---+|(?=RUNSHEET:)"))
        val parsedRunsheets = mutableListOf<Runsheet>()

        for ((index, block) in runsheetBlocks.withIndex()) {
            if (block.isBlank()) continue
            val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            var title = "Imported Runsheet ${index + 1}"
            var date = "2025-05-20"
            var driverName = "Assigned Driver"
            val addresses = mutableListOf<Address>()

            for (line in lines) {
                when {
                    line.startsWith("TITLE:", ignoreCase = true) -> {
                        title = line.substringAfter(":").trim()
                    }
                    line.startsWith("DATE:", ignoreCase = true) -> {
                        date = line.substringAfter(":").trim()
                    }
                    line.startsWith("DRIVER:", ignoreCase = true) -> {
                        driverName = line.substringAfter(":").trim()
                    }
                    line.startsWith("RUNSHEET:", ignoreCase = true) -> {
                        title = line.substringAfter(":").trim()
                    }
                    else -> {
                        val address = parseAddressLine(line, addresses.size + 1)
                        if (address != null) {
                            addresses.add(address)
                        }
                    }
                }
            }

            if (addresses.isNotEmpty()) {
                val totalDistance = addresses.size * 3.2
                val estDuration = addresses.size * 15
                parsedRunsheets.add(
                    Runsheet(
                        id = "run_imported_${System.currentTimeMillis()}_$index",
                        title = title,
                        date = date,
                        driverName = driverName,
                        status = RunsheetStatus.PENDING,
                        addresses = addresses,
                        totalDistanceKm = totalDistance,
                        estimatedDurationMinutes = estDuration
                    )
                )
            }
        }

        if (parsedRunsheets.isEmpty()) {
            val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
            val fallbackAddresses = mutableListOf<Address>()
            for ((idx, line) in lines.withIndex()) {
                val addr = parseAddressLine(line, idx + 1)
                if (addr != null) fallbackAddresses.add(addr)
            }
            if (fallbackAddresses.isNotEmpty()) {
                parsedRunsheets.add(
                    Runsheet(
                        id = "run_imported_${System.currentTimeMillis()}",
                        title = "Parsed Delivery Runsheet",
                        date = "2025-05-20",
                        driverName = "Primary Driver",
                        status = RunsheetStatus.PENDING,
                        addresses = fallbackAddresses,
                        totalDistanceKm = fallbackAddresses.size * 3.5,
                        estimatedDurationMinutes = fallbackAddresses.size * 15
                    )
                )
            }
        }

        return parsedRunsheets
    }

    private fun parseAddressLine(line: String, index: Int): Address? {
        val parts = when {
            line.contains("|") -> line.split("|").map { it.trim() }
            line.contains(";") -> line.split(";").map { it.trim() }
            else -> line.split(",").map { it.trim() }
        }

        if (parts.isEmpty()) return null

        val street = parts.getOrNull(0) ?: "Street $index"
        val city = parts.getOrNull(1) ?: "San Francisco, CA"
        val postalCode = parts.getOrNull(2) ?: "94105"
        val contactName = parts.getOrNull(3) ?: "Contact $index"
        val phoneNumber = parts.getOrNull(4) ?: "415-555-0100"
        val notes = parts.getOrNull(5) ?: ""

        val lat = 37.7749 + (index * 0.008)
        val lon = -122.4194 + (index * 0.008)

        return Address(
            id = "addr_parsed_${System.currentTimeMillis()}_$index",
            street = street,
            city = city,
            postalCode = postalCode,
            contactName = contactName,
            phoneNumber = phoneNumber,
            deliveryNotes = notes,
            latitude = lat,
            longitude = lon,
            isCompleted = false,
            priority = index
        )
    }
}
