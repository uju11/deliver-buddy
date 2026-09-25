package com.example.deliverybuddy.service

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deliverybuddy.MainActivity
import com.example.deliverybuddy.data.DeliveryRepository
import com.example.deliverybuddy.model.Address
import com.example.deliverybuddy.model.RunsheetStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.util.Locale

class FloatingWidgetService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val repository = DeliveryRepository()

    companion object {
        var isRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForeground(1001, createNotification())
        setupFloatingWindow()
    }

    private fun createNotification(): Notification {
        val channelId = "floating_widget_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Delivery Buddy Floating Widget",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Delivery Buddy Active")
                .setContentText("Floating navigation overlay is running")
                .setSmallIcon(R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Delivery Buddy Active")
                .setContentText("Floating navigation overlay is running")
                .setSmallIcon(R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 150
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        val composeView = ComposeView(this).apply {
            setContent {
                MaterialTheme(
                    colorScheme = lightColorScheme()
                ) {
                    FloatingWidgetContent(
                        repository = repository,
                        onClose = { stopSelf() },
                        onNavigate = { address ->
                            val gmmIntentUri = Uri.parse("google.navigation:q=${address.latitude},${address.longitude}&mode=d")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                setPackage("com.google.android.apps.maps")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                startActivity(mapIntent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${address.latitude},${address.longitude}")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(browserIntent)
                            }
                        },
                        onMarkDelivered = { runsheetId, addressId ->
                            repository.updateAddressStatus(runsheetId, addressId, true)
                        }
                    )
                }
            }
        }

        floatingView = composeView
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            try {
                windowManager.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun FloatingWidgetContent(
    repository: DeliveryRepository,
    onClose: () -> Unit,
    onNavigate: (Address) -> Unit,
    onMarkDelivered: (String, String) -> Unit
) {
    val context = LocalContext.current
    val runsheets by repository.runsheets.collectAsState()
    val mileage by repository.vehicleMileage.collectAsState()
    val fuelPrice by repository.fuelPrice.collectAsState()

    val activeRunsheet = runsheets.find { it.status == RunsheetStatus.IN_PROGRESS } ?: runsheets.firstOrNull()
    val totalStops = activeRunsheet?.addresses?.size ?: 0
    val completedStops = activeRunsheet?.addresses?.count { it.isCompleted } ?: 0
    val nextStop = activeRunsheet?.addresses?.firstOrNull { !it.isCompleted }
    val totalKm = activeRunsheet?.totalDistanceKm ?: 0.0
    val fuelCost = if (mileage > 0f) (totalKm / mileage) * fuelPrice else 0.0

    Surface(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Delivery Buddy Overlay",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Capture Deliveries Button in Floating Widget
            Button(
                onClick = {
                    val (_, message) = EkartAccessibilityService.captureOneShot(repository)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Capture Deliveries", style = MaterialTheme.typography.labelMedium)
            }

            if (activeRunsheet == null || nextStop == null) {
                Text(
                    text = "All deliveries completed!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress: $completedStops/$totalStops stops",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalKm km • $${String.format(Locale.getDefault(), "%.2f", fuelCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { if (totalStops > 0) completedStops.toFloat() / totalStops else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "NEXT: ${nextStop.street}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${nextStop.city} • ${nextStop.contactName} (${nextStop.phoneNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (nextStop.deliveryNotes.isNotBlank()) {
                            Text(
                                text = "Note: ${nextStop.deliveryNotes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate(nextStop) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onMarkDelivered(activeRunsheet.id, nextStop.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Done", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
