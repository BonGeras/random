package com.example.diarys22387.ui.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onLocationSelected: ((Double, Double) -> Unit)? = null
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    var mapLoaded by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }
    
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    
    val cameraPosState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(55.7558, 37.6173), 10f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPosState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionState.status is PermissionStatus.Granted
            ),
            onMapLoaded = { mapLoaded = true },
            onMapClick = { latLng ->
                selectedLocation = latLng
            }
        ) {
            notes.forEach { note ->
                if (note.hasLocation()) {
                    Marker(
                        state = MarkerState(position = LatLng(note.latitude!!, note.longitude!!)),
                        title = note.title,
                        snippet = note.content
                    )
                }
            }
            selectedLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Selected Location"
                )
            }
        }
        
        if (!mapLoaded && mapError == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }
        
        mapError?.let { error ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please make sure you have added a valid Google Maps API key",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (onLocationSelected != null && selectedLocation != null) {
            Button(
                onClick = {
                    selectedLocation?.let { location ->
                        onLocationSelected(location.latitude, location.longitude)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Select This Location")
            }
        }

        if (locationPermissionState.status !is PermissionStatus.Granted) {
            Button(
                onClick = { locationPermissionState.launchPermissionRequest() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Text("Grant Location Permission")
            }
        }
    }
}
