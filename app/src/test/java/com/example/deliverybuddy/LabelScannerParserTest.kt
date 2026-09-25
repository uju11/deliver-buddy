package com.example.deliverybuddy

import com.example.deliverybuddy.data.DeliveryRepository
import com.example.deliverybuddy.data.LabelScannerParser
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LabelScannerParserTest {

    @Before
    fun setUp() {
        DeliveryRepository().resetForTesting()
    }

    @Test
    fun testParseLabelText() {
        val rawLabel = """
            Ekart Logistics Express
            AWB: EKART123456789
            Consignee: Sarah Connor
            Phone: 4155550199
            Address: 742 Evergreen Terrace, San Francisco CA 94105
        """.trimIndent()

        val scanned = LabelScannerParser.parseLabelText(rawLabel)
        assertEquals("EKART123456789", scanned.awb)
        assertEquals("Sarah Connor", scanned.address.contactName)
        assertEquals("4155550199", scanned.address.phoneNumber)
        assertTrue(scanned.address.deliveryNotes.contains("EKART123456789"))
    }

    @Test
    fun testRepositoryAddScannedParcel() {
        val repository = DeliveryRepository()
        val rawLabel = """
            AWB: TRACK999888
            To: John Wick
            999 Continental Blvd, San Francisco CA
            5105550123
        """.trimIndent()

        val scanned = LabelScannerParser.parseLabelText(rawLabel)
        val initialCount = repository.runsheets.value.sumOf { it.addresses.size }

        repository.addAddressToActiveRunsheet(scanned.address)

        val updatedCount = repository.runsheets.value.sumOf { it.addresses.size }
        assertEquals(initialCount + 1, updatedCount)
    }
}
