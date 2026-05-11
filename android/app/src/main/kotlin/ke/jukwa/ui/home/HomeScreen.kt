package ke.jukwa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.jukwa.core.util.DeviceTier
import ke.jukwa.core.util.DeviceTierManager
import ke.jukwa.ui.theme.Amber
import ke.jukwa.ui.theme.DeepGreen
import ke.jukwa.ui.theme.EmergencyRed
import ke.jukwa.ui.theme.Green700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    deviceTierManager: DeviceTierManager,
    onNavigateToReport: () -> Unit = {},
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToBaraza: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val deviceTier = remember { deviceTierManager.getTier() }

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
            MapLayer(tier = deviceTier)

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
private fun MapLayer(tier: DeviceTier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MAPLIBRE: ${if (tier == DeviceTier.HIGH) "3D MODE" else "PERFORMANCE MODE"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
