package ke.jukwa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ke.jukwa.core.util.DeviceTier
import ke.jukwa.core.util.DeviceTierManager
import ke.jukwa.ui.theme.JukwaTheme
import ke.jukwa.ui.theme.NeonGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val deviceTier = remember { DeviceTierManager.getTier() }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Navigate to Report */ },
                containerColor = NeonGreen,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Report Incident")
            }
        },
        bottomBar = {
            HomeBottomNav()
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            
            // 1. MAP LAYER (Placeholder for MapLibre)
            MapLayer(tier = deviceTier)

            // 2. TOP HUD (Head-Up Display)
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
            
            // 3. AGENTIC OVERLAY (SDUI Placeholder)
            // This would be triggered by AI events
        }
    }
}

@Composable
fun MapLayer(tier: DeviceTier) {
    // Logic: If tier is LOW, use static vector map. If HIGH, use 3D extrusions and shadows.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "MAPLIBRE ENGINE: ${if (tier == DeviceTier.HIGH) "3D MODE" else "PERFORMANCE MODE"}",
            color = Color.DarkGray
        )
    }
}

@Composable
fun TopActionBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NeonGreen)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "STATUS: INCOGNITO",
                style = MaterialTheme.typography.labelSmall,
                color = NeonGreen
            )
            Text(
                text = "WARD: KIBRA (Nairobi)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = NeonGreen)
        }
    }
}

@Composable
fun QuickMetricsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricBadge("AIR: 45 AQI", Color(0xFF00FF00))
        MetricBadge("TRAFFIC: HIGH", Color(0xFFFFCC00))
    }
}

@Composable
fun MetricBadge(label: String, color: Color) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
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
fun HomeBottomNav() {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = NeonGreen,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Map") },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonGreen,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        // Additional items: BARAZA, Vault, Settings
    }
}
