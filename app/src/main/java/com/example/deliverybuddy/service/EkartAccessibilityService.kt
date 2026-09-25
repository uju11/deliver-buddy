package com.example.deliverybuddy.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
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

    private val repository = DeliveryRepository()
    private var lastCapturedHash = 0
    private var lastCaptureTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: ""
        
        val rootNode = rootInActiveWindow ?: return
        val textList = mutableListOf<String>()
        traverseNode(rootNode, textList)

        if (textList.isEmpty()) return

        val combinedText = textList.joinToString("\n")

        // Check if package is Ekart or content has delivery indicators (phone numbers, address keywords)
        val isEkartPackage = packageName.contains("ekart", ignoreCase = true) || 
                            packageName.contains("flipkart", ignoreCase = true) ||
                            packageName.contains("delivery", ignoreCase = true)

        val hasPhone = Regex("\\b(\\d{10}|\\d{3}[-.]?\\d{3}[-.]?\\d{4})\\b").containsMatchIn(combinedText)
        val hasAddressKeyword = listOf("st", "street", "road", "ave", "block", "nagar", "colony", "sector", "apartment", "floor", "near", "opp", "pincode", "order", "delivery", "consignee").any { 
            combinedText.contains(it, ignoreCase = true) 
        }

        if (isEkartPackage || hasPhone || hasAddressKeyword) {
            val contentHash = combinedText.hashCode()
            val currentTime = System.currentTimeMillis()
            // Throttle duplicate captures within 8 seconds
            if (contentHash != lastCapturedHash || (currentTime - lastCaptureTime > 8000)) {
                lastCapturedHash = contentHash
                lastCaptureTime = currentTime

                parseAndStoreAutoCapturedData(combinedText, packageName)
            }
        }
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

    private fun parseAndStoreAutoCapturedData(rawText: String, packageName: String) {
        val parsedRunsheets = RunsheetParser.parseRunsheets(rawText)
        if (parsedRunsheets.isNotEmpty()) {
            val ekartTitle = if (packageName.isNotBlank()) "Ekart Auto-Captured ($packageName)" else "Ekart Auto-Captured Runsheet"
            val updatedRunsheets = parsedRunsheets.map { it.copy(title = ekartTitle) }
            repository.addRunsheets(updatedRunsheets)
        } else {
            val lines = rawText.lines().map { it.trim() }.filter { it.length > 3 }
            if (lines.isNotEmpty()) {
                val street = lines.getOrNull(0) ?: "Auto-Captured Location"
                val contact = lines.find { it.contains("name", true) || it.contains("customer", true) } ?: lines.getOrNull(1) ?: "Ekart Customer"
                val phone = Regex("\\b(\\d{10}|\\d{3}[-.]?\\d{3}[-.]?\\d{4})\\b").find(rawText)?.value ?: "415-555-0100"
                
                val address = Address(
                    id = "addr_ekart_${System.currentTimeMillis()}",
                    street = street,
                    city = "San Francisco, CA",
                    postalCode = "94105",
                    contactName = contact,
                    phoneNumber = phone,
                    deliveryNotes = "Auto-captured via Ekart Accessibility Service",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    isCompleted = false,
                    priority = 1
                )

                val runsheet = Runsheet(
                    id = "run_ekart_${System.currentTimeMillis()}",
                    title = "Ekart Field X Auto-Capture",
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    driverName = "Ekart Field Driver",
                    status = RunsheetStatus.PENDING,
                    addresses = listOf(address),
                    totalDistanceKm = 4.5,
                    estimatedDurationMinutes = 15
                )
                repository.addRunsheets(listOf(runsheet))
            }
        }
    }

    override fun onInterrupt() {
        // Required override for AccessibilityService
    }
}
