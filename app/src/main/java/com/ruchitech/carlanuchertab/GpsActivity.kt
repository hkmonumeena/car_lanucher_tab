package com.ruchitech.carlanuchertab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.Granularity
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GpsActivity : ComponentActivity() {
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    LocalContext.current
    val locationState = rememberVehicleLocationState()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // Request location permission
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
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
fun rememberVehicleLocationState(): LocationState {
    val context = LocalContext.current
    val locationState = remember { LocationState() }

    DisposableEffect(Unit) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request accurate location updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).apply {
            setMinUpdateIntervalMillis(500)
            setMinUpdateDistanceMeters(2f)
            setWaitForAccurateLocation(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setGranularity(Granularity.GRANULARITY_FINE)
            }
        }.build()

        // GNSS Satellite tracking
        val gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val satellitesInUse = (0 until status.satelliteCount).count { status.usedInFix(it) }
                locationState.satellites = satellitesInUse
            }
        }

        // Location update callback
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Stricter filters for better accuracy
                    if (location.accuracy < 15f &&
                        location.hasSpeed() &&
                        location.speedAccuracyMetersPerSecond < 1.5f
                    ) {
                        locationState.updateLocation(location)
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    locationState.reset()
                }
            }
        }

        if (hasLocationPermission(context)) {
            try {
                // Request updates
                locationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                locationManager.registerGnssStatusCallback(
                    gnssCallback,
                    Handler(Looper.getMainLooper())
                )
            } catch (e: Exception) {
                Log.e("Location", "Error requesting location updates", e)
            }
        }

        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
            locationManager.unregisterGnssStatusCallback(gnssCallback)
        }
    }

    return locationState
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

class LocationState {
    var currentLocation by mutableStateOf<Location?>(null)
    var speed by mutableStateOf(0f)
    var speedAccuracy by mutableStateOf(0f)
    var hasFix by mutableStateOf(false)
    var gpsAccuracy by mutableStateOf(0f)
    var satellites by mutableStateOf(0)

    private var lastSmoothedSpeed = 0f

    fun updateLocation(location: Location) {
        currentLocation = location

        // Convert speed (m/s to km/h)
        val newSpeed = location.speed * 3.6f
        val alpha = 0.3f // smoothing factor
        lastSmoothedSpeed = alpha * newSpeed + (1 - alpha) * lastSmoothedSpeed
        speed = lastSmoothedSpeed

        speedAccuracy = location.speedAccuracyMetersPerSecond * 3.6f
        gpsAccuracy = location.accuracy
        hasFix = gpsAccuracy < 15f && speedAccuracy < 2f

        // Satellites handled in GNSS callback
    }

    fun reset() {
        currentLocation = null
        speed = 0f
        hasFix = false
        gpsAccuracy = 0f
        speedAccuracy = 0f
        lastSmoothedSpeed = 0f
        satellites = 0
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