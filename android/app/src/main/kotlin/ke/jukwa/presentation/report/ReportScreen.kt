package ke.jukwa.presentation.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ke.jukwa.ui.theme.DeepGreen
import ke.jukwa.ui.theme.EmergencyRed
import ke.jukwa.ui.theme.Green700

private val INCIDENT_CATEGORIES = listOf(
    "Robbery" to "robbery",
    "Assault" to "assault",
    "Theft" to "theft",
    "Suspicious Activity" to "suspicious_activity",
    "Pothole" to "pothole",
    "Congestion" to "congestion",
    "Accident" to "accident",
    "Broken Water Main" to "broken_water_main",
    "No Electricity" to "no_electricity",
    "Damaged Streetlight" to "damaged_streetlight",
    "Drug Shortage" to "drug_shortage",
    "Waste Management" to "waste_management",
    "Illegal Dumping" to "illegal_dumping",
    "Noise Pollution" to "noise_pollution",
    "Fire" to "fire",
    "Medical Emergency" to "medical_emergency",
    "Other" to "other",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            snackbarHostState.showSnackbar("Report submitted! It will sync when online.")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Incident") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isSubmitted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Report Submitted",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Green700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your report has been saved and will sync when you have connectivity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.resetState()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Green700)
                    ) {
                        Text("Report Another")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "What happened?",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    INCIDENT_CATEGORIES.forEach { (label, value) ->
                        FilterChip(
                            selected = uiState.category == value,
                            onClick = { viewModel.updateCategory(value) },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Text(
                    text = "Severity: ${uiState.severity}/5",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = uiState.severity.toFloat(),
                    onValueChange = { viewModel.updateSeverity(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = if (uiState.severity >= 4) EmergencyRed else Green700,
                        activeTrackColor = if (uiState.severity >= 4) EmergencyRed else Green700,
                    )
                )

                var descriptionText by remember { mutableStateOf(TextFieldValue(uiState.description)) }
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it
                        viewModel.updateDescription(it.text)
                    },
                    label = { Text("Description") },
                    placeholder = { Text("Describe what happened...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )

                Text(
                    text = "Location: ${String.format("%.4f", uiState.latitude)}, ${String.format("%.4f", uiState.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.submitReport() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting && uiState.category.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(if (uiState.isSubmitting) "Submitting..." else "Submit Report")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
