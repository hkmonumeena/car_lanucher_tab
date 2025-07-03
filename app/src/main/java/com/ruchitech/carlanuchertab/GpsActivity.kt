package com.ruchitech.carlanuchertab

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.ruchitech.carlanuchertab.ui.theme.CarLanucherTabTheme

class GpsActivity : ComponentActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize MapView
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        setContent {
            CarLanucherTabTheme {
                GPSMapScreen(mapView)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSMapScreen(mapView: MapView) {
    val context = LocalContext.current
    val locationState = rememberLocationState()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // Request location permission
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Map View
            AndroidView(
                factory = { mapView },
                update = { view ->
                    view.getMapAsync { map ->
                        googleMap = map
                        map.uiSettings.isMyLocationButtonEnabled = false
                        map.uiSettings.isZoomControlsEnabled = false
                        map.isTrafficEnabled = true

                        // Enable my location layer if permission granted
                        if (locationPermissionState.status.isGranted) {
                            map.isMyLocationEnabled = true
                        }
                    }
                }
            )

            // Update camera position when location changes
            locationState.currentLocation?.let { location ->
                LaunchedEffect(location) {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            18f // Zoom level (higher = more zoomed in)
                        )
                    )
                }
            }

            // Rest of your UI (speed display, etc.)
            SpeedDisplay(
                speed = locationState.speed,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun rememberLocationState(): LocationState {
    val context = LocalContext.current
    val locationState = remember { LocationState() }

    DisposableEffect (Unit) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000 // Update interval in ms
        ).apply {
            setMinUpdateIntervalMillis(500)
            setWaitForAccurateLocation(true)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { location ->
                    locationState.updateLocation(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                locationState.hasFix = availability.isLocationAvailable
            }
        }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }

    return locationState
}


class LocationState {
    var currentLocation by mutableStateOf<Location?>(null)
    var speed by mutableStateOf(0f)  // in m/s
    var hasFix by mutableStateOf(false)

    fun updateLocation(location: Location) {
        currentLocation = location
        speed = location.speed
        hasFix = location.accuracy < 20f  // Considered good fix if accuracy < 20m
    }
}

@Composable
fun SpeedDisplay(speed: Float, modifier: Modifier = Modifier) {
    val kmhSpeed = speed * 3.6f // Convert m/s to km/h

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = CircleShape
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "%.0f".format(kmhSpeed),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun GPSStatusIndicator(hasFix: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = if (hasFix) Color.Green.copy(alpha = 0.7f)
                else Color.Red.copy(alpha = 0.7f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "GPS Status",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}