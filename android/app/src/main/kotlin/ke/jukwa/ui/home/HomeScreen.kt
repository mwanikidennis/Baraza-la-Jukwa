package ke.jukwa.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationResult
import ke.jukwa.core.util.DeviceTier
import ke.jukwa.core.util.DeviceTierManager
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.presentation.home.MapViewModel
import ke.jukwa.ui.theme.Amber
import ke.jukwa.ui.theme.Green700
import ke.jukwa.ui.theme.JukwaTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun HomeScreen(
    deviceTierManager: DeviceTierManager,
    onNavigateToReport: () -> Unit = {},
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToBaraza: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val deviceTier = remember { deviceTierManager.tier }
    val context = LocalContext.current
    val location by mapViewModel.location.collectAsState()
    val incidents by mapViewModel.incidents.collectAsState()

    // Initialize MapLibre
    remember {
        MapLibre.getInstance(context)
    }

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val areGranted = permissions.values.all { it }
        if (areGranted) {
            mapViewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsGranted = locationPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (permissionsGranted) {
            mapViewModel.startLocationUpdates()
        } else {
            launcher.launch(locationPermissions)
        }
    }

    HomeScreenContent(
        deviceTier = deviceTier,
        location = location,
        incidents = incidents,
        onNavigateToReport = onNavigateToReport,
        onNavigateToMyReports = onNavigateToMyReports,
        onNavigateToBaraza = onNavigateToBaraza,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
fun HomeScreenContent(
    deviceTier: DeviceTier,
    location: LocationResult?,
    incidents: List<IncidentEntity>,
    onNavigateToReport: () -> Unit = {},
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToBaraza: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToReport,
                containerColor = Green700,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Report Incident")
            }
        },
        bottomBar = {
            HomeBottomNav(
                selectedTab = HomeTab.MAP,
                onTabSelected = { tab ->
                    when (tab) {
                        HomeTab.MAP -> {}
                        HomeTab.MY_REPORTS -> onNavigateToMyReports()
                        HomeTab.BARAZA -> onNavigateToBaraza()
                        HomeTab.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            MapLayer(tier = deviceTier, location = location, incidents = incidents)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                TopActionBar()
                Spacer(modifier = Modifier.height(8.dp))
                QuickMetricsRow()
            }
        }
    }
}

enum class HomeTab {
    MAP, MY_REPORTS, BARAZA, SETTINGS
}


@Composable
private fun MapLayer(tier: DeviceTier, location: LocationResult?, incidents: List<IncidentEntity>) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    if (isPreview) {
        // Simple placeholder for Preview mode to avoid MapLibre initialization issues
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Map Preview (MapLibre)", color = Color.DarkGray)
        }
        return
    }

    val mapView = rememberMapViewWithLifecycle()
    
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize()
    ) { map ->
        map.getMapAsync { maplibreMap ->
            maplibreMap.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                maplibreMap.removeAnnotations() // Clear previous markers
                incidents.forEach { incident ->
                    maplibreMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(incident.latitude, incident.longitude))
                            .title(incident.incidentCategory)
                            .snippet(incident.description)
                    )
                }
            }

            location?.lastLocation?.let { userLocation ->
                val position = CameraPosition.Builder()
                    .target(LatLng(userLocation.latitude, userLocation.longitude))
                    .zoom(14.0)
                    .build()
                maplibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
            } ?: run {
                if (maplibreMap.cameraPosition.zoom < 2.0) { // Only set initial if not already moved
                    val position = CameraPosition.Builder()
                        .target(LatLng(-1.286389, 36.817223)) // Nairobi
                        .zoom(12.0)
                        .build()
                    maplibreMap.cameraPosition = position
                }
            }
        }
    }

    LaunchedEffect(location) {
        location?.lastLocation?.let { userLocation ->
            mapView.getMapAsync { map ->
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.latitude, userLocation.longitude), 14.0)
                )
            }
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return mapView
}


@Composable
private fun TopActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Green700)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "STATUS: STANDARD",
                style = MaterialTheme.typography.labelSmall,
                color = Green700
            )
            Text(
                text = "WARD: KIBRA (Nairobi)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Alerts",
                tint = Amber
            )
        }
    }
}

@Composable
private fun QuickMetricsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBadge("AIR: 45 AQI", Green700)
        MetricBadge("TRAFFIC: HIGH", Amber)
    }
}

@Composable
private fun MetricBadge(label: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun HomeBottomNav(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Green700,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Map") },
            selected = selectedTab == HomeTab.MAP,
            onClick = { onTabSelected(HomeTab.MAP) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green700,
                selectedTextColor = Green700,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Green700.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("Reports") },
            selected = selectedTab == HomeTab.MY_REPORTS,
            onClick = { onTabSelected(HomeTab.MY_REPORTS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green700,
                selectedTextColor = Green700,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Green700.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Baraza") },
            selected = selectedTab == HomeTab.BARAZA,
            onClick = { onTabSelected(HomeTab.BARAZA) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green700,
                selectedTextColor = Green700,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Green700.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = selectedTab == HomeTab.SETTINGS,
            onClick = { onTabSelected(HomeTab.SETTINGS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Green700,
                selectedTextColor = Green700,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Green700.copy(alpha = 0.12f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JukwaTheme {
        HomeScreenContent(
            deviceTier = DeviceTier.HIGH,
            location = null,
            incidents = emptyList()
        )
    }
}
