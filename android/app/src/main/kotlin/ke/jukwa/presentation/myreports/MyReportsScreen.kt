package ke.jukwa.presentation.myreports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.ui.theme.Amber
import ke.jukwa.ui.theme.EmergencyRed
import ke.jukwa.ui.theme.Green700
import ke.jukwa.ui.theme.Gray600
import ke.jukwa.ui.theme.JukwaTheme

@Composable
fun MyReportsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MyReportsViewModel = hiltViewModel(),
) {
    val incidents by viewModel.incidents.collectAsState()

    MyReportsScreenContent(
        incidents = incidents,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreenContent(
    incidents: List<IncidentEntity>,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No reports yet",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to report your first incident",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(incidents, key = { it.incidentId }) { incident ->
                    IncidentCard(incident)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun IncidentCard(incident: IncidentEntity) {
    val statusColor = when (incident.status) {
        "QUEUED_FOR_SYNC" -> Amber
        "SUBMITTED" -> Green700
        "ACKNOWLEDGED" -> Green700
        "IN_PROGRESS" -> Green700
        "RESOLVED" -> Green700
        "EMERGENCY" -> EmergencyRed
        else -> Gray600
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = incident.incidentCategory.replace("_", " ")
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = incident.status.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (incident.description.isNotBlank()) {
                Text(
                    text = incident.description.take(120) + if (incident.description.length > 120) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Severity: ${incident.severityScore}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (incident.isSynced) "Synced" else "Pending sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (incident.isSynced) Green700 else Amber
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyReportsScreenPreview() {
    JukwaTheme {
        MyReportsScreenContent(
            incidents = listOf(
                IncidentEntity(
                    incidentId = "1",
                    incidentCategory = "robbery",
                    description = "Happened at the corner of 5th and 10th.",
                    latitude = -1.286389,
                    longitude = 36.817223,
                    severityScore = 4,
                    status = "SUBMITTED",
                    isSynced = true,
                    timestamp = System.currentTimeMillis()
                ),
                IncidentEntity(
                    incidentId = "2",
                    incidentCategory = "pothole",
                    description = "Large pothole in the middle of the road.",
                    latitude = -1.286389,
                    longitude = 36.817223,
                    severityScore = 2,
                    status = "QUEUED_FOR_SYNC",
                    isSynced = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        )
    }
}
