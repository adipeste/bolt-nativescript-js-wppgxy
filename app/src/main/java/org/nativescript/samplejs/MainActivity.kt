package org.nativescript.samplejs

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    // ... (previous code remains the same)

    @Composable
    fun GPSAppContent(mapView: MapView) {
        val viewModel: GPSViewModel = viewModel()
        val routeViewModel: RouteViewModel = viewModel()
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("MyRoute") },
                    actions = {
                        Text(viewModel.gpsSignal.value)
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }
                ) {
                    Text("⚙️")
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )
                
                val navigationInstructions by routeViewModel.navigationInstructions.collectAsState()
                val nextTurn by routeViewModel.nextTurn.collectAsState()
                val estimatedArrivalTime by routeViewModel.estimatedArrivalTime.collectAsState()
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Next turn: $nextTurn",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "ETA: $estimatedArrivalTime",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(navigationInstructions) { instruction ->
                            Text(
                                text = instruction.mInstructions,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (scaffoldState.drawerState.isOpen) {
            SettingsDrawer(
                onClose = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.close()
                    }
                },
                routeViewModel = routeViewModel
            )
        }
    }

    @Composable
    fun SettingsDrawer(onClose: () -> Unit, routeViewModel: RouteViewModel) {
        var destination by remember { mutableStateOf("") }
        var vehicleType by remember { mutableStateOf(VehicleType.CAR) }
        var avoidTolls by remember { mutableStateOf(false) }
        var avoidHighways by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Settings", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    routeViewModel.searchAndSetDestination(destination)
                    onClose()
                }
            ) {
                Text("Set Destination")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Vehicle Type", style = MaterialTheme.typography.subtitle1)
            Row {
                RadioButton(
                    selected = vehicleType == VehicleType.CAR,
                    onClick = { vehicleType = VehicleType.CAR }
                )
                Text("Car")
                RadioButton(
                    selected = vehicleType == VehicleType.TRUCK,
                    onClick = { vehicleType = VehicleType.TRUCK }
                )
                Text("Truck")
                RadioButton(
                    selected = vehicleType == VehicleType.BICYCLE,
                    onClick = { vehicleType = VehicleType.BICYCLE }
                )
                Text("Bicycle")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Checkbox(
                    checked = avoidTolls,
                    onCheckedChange = { avoidTolls = it }
                )
                Text("Avoid Tolls")
            }
            Row {
                Checkbox(
                    checked = avoidHighways,
                    onCheckedChange = { avoidHighways = it }
                )
                Text("Avoid Highways")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    routeViewModel.setUserPreferences(UserPreferences(vehicleType, avoidTolls, avoidHighways))
                    onClose()
                }
            ) {
                Text("Apply Settings")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    routeViewModel.cancelRoute()
                    onClose()
                }
            ) {
                Text("Cancel Route")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    routeViewModel.openFavoritesScreen()
                    onClose()
                }
            ) {
                Text("Manage Favorites")
            }
        }
    }
}