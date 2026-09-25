package com.example.deliverybuddy.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deliverybuddy.model.Address
import com.example.deliverybuddy.model.PetrolPump
import com.example.deliverybuddy.model.RoutePlan
import com.example.deliverybuddy.service.FloatingWidgetService
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutePlanScreen(
    routePlan: RoutePlan?,
    allAddresses: List<Address>,
    nearbyPetrolPumps: List<PetrolPump>,
    vehicleMileage: Float,
    fuelPrice: Float,
    onRecalculateRoute: (String?, String?) -> RoutePlan?,
    onAddPetrolStop: (PetrolPump, String?, String?) -> RoutePlan?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedStartId by remember { mutableStateOf<String?>(null) }
    var selectedEndId by remember { mutableStateOf<String?>(null) }
    var currentRoutePlan by remember { mutableStateOf(routePlan) }

    val totalKm = currentRoutePlan?.totalDistanceKm ?: 0.0
    val fuelCost = if (vehicleMileage > 0f) (totalKm / vehicleMileage) * fuelPrice else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route Optimization & Fuel Cost", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        if (currentRoutePlan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Route plan not available.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Optimized Route Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "${currentRoutePlan?.optimizedStops?.size} Stops",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Total Distance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$totalKm km", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Estimated Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${currentRoutePlan?.estimatedDurationMinutes} mins", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "Projected Fuel Cost", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$${String.format(Locale.getDefault(), "%.2f", fuelCost)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Route Start & End Selection", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var startExpanded by remember { mutableStateOf(false) }
                            val startAddressName = allAddresses.find { it.id == selectedStartId }?.street ?: "Select Start (Depot)"

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { startExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = startAddressName, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                                }
                                DropdownMenu(
                                    expanded = startExpanded,
                                    onDismissRequest = { startExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Auto (First Stop)") },
                                        onClick = {
                                            selectedStartId = null
                                            startExpanded = false
                                            currentRoutePlan = onRecalculateRoute(null, selectedEndId) ?: currentRoutePlan
                                        }
                                    )
                                    allAddresses.forEach { addr ->
                                        DropdownMenuItem(
                                            text = { Text(addr.street) },
                                            onClick = {
                                                selectedStartId = addr.id
                                                startExpanded = false
                                                currentRoutePlan = onRecalculateRoute(addr.id, selectedEndId) ?: currentRoutePlan
                                            }
                                        )
                                    }
                                }
                            }

                            var endExpanded by remember { mutableStateOf(false) }
                            val endAddressName = allAddresses.find { it.id == selectedEndId }?.street ?: "Select End (Optional)"

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { endExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = endAddressName, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                                }
                                DropdownMenu(
                                    expanded = endExpanded,
                                    onDismissRequest = { endExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None (Auto End)") },
                                        onClick = {
                                            selectedEndId = null
                                            endExpanded = false
                                            currentRoutePlan = onRecalculateRoute(selectedStartId, null) ?: currentRoutePlan
                                        }
                                    )
                                    allAddresses.forEach { addr ->
                                        DropdownMenuItem(
                                            text = { Text(addr.street) },
                                            onClick = {
                                                selectedEndId = addr.id
                                                endExpanded = false
                                                currentRoutePlan = onRecalculateRoute(selectedStartId, addr.id) ?: currentRoutePlan
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(context, FloatingWidgetService::class.java)
                                if (FloatingWidgetService.isRunning) {
                                    context.stopService(intent)
                                } else {
                                    if (Settings.canDrawOverlays(context)) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    } else {
                                        val permIntent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(permIntent)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Rounded.Layers, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (FloatingWidgetService.isRunning) "Stop Floating Widget Overlay" else "Launch Floating Widget Overlay")
                        }
                    }
                }

                // Petrol Pump Detection Banner & Action
                if (nearbyPetrolPumps.isNotEmpty()) {
                    var selectedPumpIndex by remember { mutableStateOf(0) }
                    val currentPump = nearbyPetrolPumps[selectedPumpIndex]
                    var petrolAddedMessage by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalGasStation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Nearby Petrol Pump Found",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "${currentPump.distanceKm} km away",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }

                            Text(
                                text = "${currentPump.name} • ${currentPump.street}\nEstimated Price: $${String.format(Locale.getDefault(), "%.2f", currentPump.pricePerLiter)}/L",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )

                            if (petrolAddedMessage != null) {
                                Text(
                                    text = petrolAddedMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val updated = onAddPetrolStop(currentPump, selectedStartId, selectedEndId)
                                        if (updated != null) {
                                            currentRoutePlan = updated
                                            petrolAddedMessage = "✓ Added '${currentPump.name}' as a stop. Distance & fuel cost updated."
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fill Petrol / Add Stop")
                                }

                                if (nearbyPetrolPumps.size > 1) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedPumpIndex = (selectedPumpIndex + 1) % nearbyPetrolPumps.size
                                            petrolAddedMessage = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Next Pump (${selectedPumpIndex + 1}/${nearbyPetrolPumps.size})")
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Optimized Stop Sequence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(currentRoutePlan?.optimizedStops.orEmpty()) { index, address ->
                        val isFuelStop = address.id.startsWith("pump_")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFuelStop)
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isFuelStop) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (isFuelStop) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        modifier = Modifier.size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isFuelStop) {
                                            Icon(
                                                imageVector = Icons.Rounded.LocalGasStation,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.onTertiary
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = address.street,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${address.city} (${address.contactName} • ${address.phoneNumber})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (address.deliveryNotes.isNotBlank()) {
                                        Text(
                                            text = "Note: ${address.deliveryNotes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isFuelStop) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isFuelStop) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
