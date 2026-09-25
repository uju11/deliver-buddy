package com.example.deliverybuddy

import com.example.deliverybuddy.data.DeliveryRepository
import com.example.deliverybuddy.data.RunsheetParser
import com.example.deliverybuddy.data.SortOrder
import org.junit.Assert.*
import org.junit.Test

class RunsheetParserTest {

    @Test
    fun testParseSingleRunsheet() {
        val rawText = """
            TITLE: Test Express
            DATE: 2025-05-21
            DRIVER: Test Driver
            123 Main St | San Francisco CA | 94105 | John Doe | 555-0100 | Leave at door
            456 Market St | San Francisco CA | 94105 | Jane Smith | 555-0200 | Ring bell
        """.trimIndent()

        val runsheets = RunsheetParser.parseRunsheets(rawText)
        assertEquals(1, runsheets.size)
        val runsheet = runsheets[0]
        assertEquals("Test Express", runsheet.title)
        assertEquals("Test Driver", runsheet.driverName)
        assertEquals(2, runsheet.addresses.size)
        assertEquals("123 Main St", runsheet.addresses[0].street)
        assertEquals("John Doe", runsheet.addresses[0].contactName)
    }

    @Test
    fun testParseMultipleRunsheets() {
        val rawText = """
            TITLE: Runsheet Alpha
            111 First St | SF CA | 94105 | Alice | 555-1111
            ---
            TITLE: Runsheet Beta
            222 Second St | SF CA | 94103 | Bob | 555-2222
        """.trimIndent()

        val runsheets = RunsheetParser.parseRunsheets(rawText)
        assertEquals(2, runsheets.size)
        assertEquals("Runsheet Alpha", runsheets[0].title)
        assertEquals("Runsheet Beta", runsheets[1].title)
    }

    @Test
    fun testRepositorySortingAndCompletion() {
        val repository = DeliveryRepository()
        val initialList = repository.runsheets.value
        assertFalse(initialList.isEmpty())
        val runsheetId = initialList[0].id

        val firstAddrId = initialList[0].addresses[0].id
        val initialStatus = initialList[0].addresses[0].isCompleted
        repository.updateAddressStatus(runsheetId, firstAddrId, !initialStatus)
        val updatedRunsheet = repository.getRunsheetById(runsheetId)
        assertNotNull(updatedRunsheet)
        val updatedAddr = updatedRunsheet?.addresses?.find { it.id == firstAddrId }
        assertEquals(!initialStatus, updatedAddr?.isCompleted)

        repository.sortRunsheetAddresses(runsheetId, SortOrder.CLOSEST_FIRST)
        val sortedRunsheet = repository.getRunsheetById(runsheetId)
        assertNotNull(sortedRunsheet)
        assertEquals(3, sortedRunsheet?.addresses?.size)
    }

    @Test
    fun testDeliveryHistoryAndFuelCostTracking() {
        val repository = DeliveryRepository()
        val initialList = repository.runsheets.value
        assertFalse(initialList.isEmpty())
        val runsheetId = initialList[0].id

        val initialHistoryCount = repository.historyRecords.value.size
        repository.completeAndArchiveRunsheet(runsheetId)

        val updatedHistory = repository.historyRecords.value
        assertTrue(updatedHistory.size > initialHistoryCount)
        val latestRecord = updatedHistory[0]
        assertEquals(runsheetId, latestRecord.runsheetId)
        assertTrue(latestRecord.projectedFuelCost >= 0.0)
        assertTrue(latestRecord.actualFuelCost >= 0.0)
    }

    @Test
    fun testPetrolPumpDetectionAndRouteRecalculation() {
        val repository = DeliveryRepository()
        val runsheets = repository.runsheets.value
        assertFalse(runsheets.isEmpty())
        val runsheetId = runsheets[0].id

        val nearbyPumps = repository.getNearbyPetrolPumps(runsheetId)
        assertFalse(nearbyPumps.isEmpty())
        val pump = nearbyPumps[0]
        assertNotNull(pump.name)
        assertTrue(pump.distanceKm > 0.0)

        val basePlan = repository.generateRoutePlan(runsheetId)
        assertNotNull(basePlan)
        val initialStopCount = basePlan!!.optimizedStops.size
        val initialDistance = basePlan.totalDistanceKm

        val updatedPlan = repository.addPetrolStopToRoutePlan(runsheetId, pump)
        assertNotNull(updatedPlan)
        assertEquals(initialStopCount + 1, updatedPlan!!.optimizedStops.size)
        assertTrue(updatedPlan.totalDistanceKm >= initialDistance)
        assertTrue(updatedPlan.optimizedStops.any { it.id == pump.id })
    }

    @Test
    fun testFuelAnalyticsComputation() {
        val repository = DeliveryRepository()
        repository.updateSettings(40.0f, 2.0f)
        val analytics = repository.getFuelAnalytics()
        assertNotNull(analytics)
        assertTrue(analytics.totalKmRun >= 0.0)
        assertTrue(analytics.dailyMileageKm >= 0.0)
        assertTrue(analytics.projectedMonthlyFuelCost >= 0.0)
        assertTrue(analytics.actualMonthlyFuelCost >= 0.0)
        assertTrue(analytics.dailyFuelCost >= 0.0)
    }
}
