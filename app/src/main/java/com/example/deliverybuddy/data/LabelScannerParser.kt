package com.example.deliverybuddy.data

import com.example.deliverybuddy.model.Address

object LabelScannerParser {
    data class ScannedParcel(
        val address: Address,
        val awb: String
    )

    fun parseLabelText(rawText: String, index: Int = 1): ScannedParcel {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        
        var street = "100 Scanned Parcel Rd"
        var city = "San Francisco, CA"
        var postalCode = "94105"
        var contactName = "Parcel Recipient"
        var phoneNumber = "415-555-0199"
        var awb = "AWB${System.currentTimeMillis().toString().takeLast(6)}"

        // Extract AWB / Tracking number
        val awbRegex = Regex("(?i)(awb|tracking|waybill|order)[\\s:]*([A-Z0-9_-]{6,})")
        val awbMatch = awbRegex.find(rawText)
        if (awbMatch != null) {
            awb = awbMatch.groupValues[2]
        } else {
            val codeRegex = Regex("\\b[A-Z0-9]{8,15}\\b")
            val codeMatch = codeRegex.find(rawText)
            if (codeMatch != null) {
                awb = codeMatch.value
            }
        }

        // Extract Phone Number
        val phoneRegex = Regex("\\b(\\d{10}|\\d{3}[-.]?\\d{3}[-.]?\\d{4})\\b")
        val phoneMatch = phoneRegex.find(rawText)
        if (phoneMatch != null) {
            phoneNumber = phoneMatch.value
        }

        // Extract Contact Name
        val nameLine = lines.find { it.contains("name", true) || it.contains("consignee", true) || it.contains("to:", true) }
        if (nameLine != null) {
            contactName = nameLine.substringAfter(":").trim().ifBlank { nameLine }
        } else if (lines.size > 1) {
            contactName = lines[1]
        }

        // Extract Street Address
        val streetLine = lines.find { 
            it.contains("st", true) || it.contains("street", true) || it.contains("road", true) || 
            it.contains("ave", true) || it.contains("block", true) || it.contains("nagar", true) 
        }
        if (streetLine != null) {
            street = streetLine
        } else if (lines.isNotEmpty()) {
            street = lines[0]
        }

        val notes = "Scanned Parcel Label • AWB: $awb"
        val address = Address(
            id = "addr_scan_${System.currentTimeMillis()}_$index",
            street = street,
            city = city,
            postalCode = postalCode,
            contactName = contactName,
            phoneNumber = phoneNumber,
            deliveryNotes = notes,
            latitude = 37.7749 + (index * 0.005),
            longitude = -122.4194 + (index * 0.005),
            isCompleted = false,
            priority = 1
        )

        return ScannedParcel(address, awb)
    }
}
