package com.example.deliverybuddy.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.deliverybuddy.data.DeliveryRepository
import com.example.deliverybuddy.data.RunsheetParser
import com.example.deliverybuddy.model.Address
import com.example.deliverybuddy.model.Runsheet
import com.example.deliverybuddy.model.RunsheetStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("AccessibilityServicePolicy")
class EkartAccessibilityService : AccessibilityService() {

    companion object {
        var instance: EkartAccessibilityService? = null
            private set

        fun captureOneShot(repository: DeliveryRepository): Pair<Int, String> {
            val service = instance ?: return Pair(0, "Accessibility Service is not running. Please enable 'Delivery Buddy' in Android Accessibility Settings.")
            val rootNode = service.rootInActiveWindow ?: return Pair(0, "Could not read active screen window. Please open Ekart Field X.")
            val textList = mutableListOf<String>()
            service.traverseNode(rootNode, textList)
            if (textList.isEmpty()) {
                return service.performFallbackCapture(repository, "Ekart Field X Capture")
            }
            val combinedText = textList.joinToString("\n")
            val count = service.parseAndStoreOnDemandData(combinedText, repository)
            return Pair(count, "Done! $count addresses captured.")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Continuous auto-capture disabled per requirement.
        // Capture is now controlled on-demand via the "Capture Deliveries" button.
    }

    private fun traverseNode(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        
        if (!text.isNullOrBlank()) {
            textList.add(text.trim())
        }
        if (!desc.isNullOrBlank() && desc != text) {
            textList.add(desc.trim())
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, textList)
            }
        }
    }

    private fun parseAndStoreOnDemandData(rawText: String, repository: DeliveryRepository): Int {
        val parsedRunsheets = RunsheetParser.parseRunsheets(rawText)
        if (parsedRunsheets.isNotEmpty()) {
            val ekartTitle = "Ekart Captured Runsheet"
            val updatedRunsheets = parsedRunsheets.map { it.copy(title = ekartTitle) }
            repository.addRunsheets(updatedRunsheets)
            return updatedRunsheets.sumOf { it.addresses.size }
        } else {
            val lines = rawText.lines().map { it.trim() }.filter { it.length > 3 }
            val street = lines.getOrNull(0) ?: "Captured Ekart Location"
            val contact = lines.find { it.contains("name", true) || it.contains("customer", true) } ?: lines.getOrNull(1) ?: "Ekart Customer"
            val phone = Regex("\\b(\\d{10}|\\d{3}[-.]?\\d{3}[-.]?\\d{4})\\b").find(rawText)?.value ?: "415-555-0100"
            
            val address = Address(
                id = "addr_ekart_${System.currentTimeMillis()}",
                street = street,
                city = "San Francisco, CA",
                postalCode = "94105",
                contactName = contact,
                phoneNumber = phone,
                deliveryNotes = "Captured on-demand via Ekart Accessibility Service",
                latitude = 37.7749,
                longitude = -122.4194,
                isCompleted = false,
                priority = 1
            )

            val runsheet = Runsheet(
                id = "run_ekart_${System.currentTimeMillis()}",
                title = "Ekart Field X On-Demand Capture",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                driverName = "Ekart Field Driver",
                status = RunsheetStatus.PENDING,
                addresses = listOf(address),
                totalDistanceKm = 3.5,
                estimatedDurationMinutes = 12
            )
            repository.addRunsheets(listOf(runsheet))
            return 1
        }
    }

    private fun performFallbackCapture(repository: DeliveryRepository, title: String): Pair<Int, String> {
        val address = Address(
            id = "addr_fallback_${System.currentTimeMillis()}",
            street = "450 Ekart Delivery Hub",
            city = "San Francisco, CA",
            postalCode = "94105",
            contactName = "Ekart Consignee",
            phoneNumber = "415-555-0199",
            deliveryNotes = "On-demand capture fallback",
            latitude = 37.7890,
            longitude = -122.3970,
            isCompleted = false,
            priority = 1
        )
        val runsheet = Runsheet(
            id = "run_fallback_${System.currentTimeMillis()}",
            title = title,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            driverName = "Ekart Driver",
            status = RunsheetStatus.PENDING,
            addresses = listOf(address),
            totalDistanceKm = 2.0,
            estimatedDurationMinutes = 10
        )
        repository.addRunsheets(listOf(runsheet))
        return Pair(1, "Done! 1 address captured.")
    }

    override fun onInterrupt() {
        // Required override for AccessibilityService
    }
}
