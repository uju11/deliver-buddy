package com.example.deliverybuddy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deliverybuddy.model.Runsheet
import com.example.deliverybuddy.model.RunsheetStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunsheetListScreen(
    runsheets: List<Runsheet>,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRunsheetClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onParseAndAddRunsheet: (String) -> Unit,
    onScanParcelLabelText: (String) -> Unit,
    onScanParcelLabelImageClick: () -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var showScanLabelDialog by remember { mutableStateOf(false) }
    var rawTextPrompt by remember { mutableStateOf("") }
    var labelTextPrompt by remember { mutableStateOf("") }

    val totalStops = runsheets.sumOf { it.addresses.size }
    val completedStops = runsheets.sumOf { r -> r.addresses.count { it.isCompleted } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Delivery Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showScanLabelDialog = true }) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Scan Parcel Label")
                    }
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Rounded.History, contentDescription = "Delivery History")
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Import Runsheet")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showScanLabelDialog = true },
                    icon = { Icon(Icons.Rounded.QrCodeScanner, contentDescription = null) },
                    text = { Text("Scan Label") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Progress Summary Banner (Minimalist)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live Delivery Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$completedStops of $totalStops Stops Completed",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        val progress = if (totalStops > 0) completedStops.toFloat() / totalStops else 0f
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (totalStops > 0) completedStops.toFloat() / totalStops else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                }
            }

            if (runsheets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No runsheets available. Tap 'Scan Label'.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(runsheets) { runsheet ->
                        RunsheetCard(runsheet = runsheet, onClick = { onRunsheetClick(runsheet.id) })
                    }
                }
            }
        }
    }

    if (showScanLabelDialog) {
        AlertDialog(
            onDismissRequest = { showScanLabelDialog = false },
            title = { Text("Parcel Delivery Label Scanner", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Capture or pick a parcel label photo, or paste label text below to extract address, name, phone, and AWB:", style = MaterialTheme.typography.bodyMedium)
                    
                    Button(
                        onClick = {
                            showScanLabelDialog = false
                            onScanParcelLabelImageClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick Label Photo / Camera")
                    }

                    HorizontalDivider()

                    Text("Or paste parcel label text:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = labelTextPrompt,
                        onValueChange = { labelTextPrompt = it },
                        placeholder = { Text("AWB: EKART987654\n123 Market St, San Francisco CA 94105\nJohn Doe\n415-555-0192") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (labelTextPrompt.isNotBlank()) {
                            onScanParcelLabelText(labelTextPrompt)
                            labelTextPrompt = ""
                            showScanLabelDialog = false
                        }
                    }
                ) {
                    Text("Extract & Add", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanLabelDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Parse & Import Runsheet", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste single or multiple runsheet dispatch text (Street, City, Zip, Contact, Phone, Notes separated by commas or pipes):", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = rawTextPrompt,
                        onValueChange = { rawTextPrompt = it },
                        placeholder = { Text("TITLE: Express Run\n123 Market St, San Francisco CA 94105 | Alice | 415-555-0192") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rawTextPrompt.isNotBlank()) {
                            onParseAndAddRunsheet(rawTextPrompt)
                            rawTextPrompt = ""
                            showImportDialog = false
                        }
                    }
                ) {
                    Text("Import", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RunsheetCard(
    runsheet: Runsheet,
    onClick: () -> Unit
) {
    val completedCount = runsheet.addresses.count { it.isCompleted }
    val totalCount = runsheet.addresses.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                        imageVector = Icons.Rounded.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = runsheet.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                RunsheetStatusChip(status = runsheet.status)
            }

            Text(
                text = "Driver: ${runsheet.driverName} • Date: ${runsheet.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Delivered: $completedCount of $totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${runsheet.estimatedDurationMinutes} mins • ${runsheet.totalDistanceKm} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RunsheetStatusChip(status: RunsheetStatus) {
    val (label, containerColor, contentColor) = when (status) {
        RunsheetStatus.PENDING -> Triple("Pending", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        RunsheetStatus.IN_PROGRESS -> Triple("In Progress", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        RunsheetStatus.COMPLETED -> Triple("Completed", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}
