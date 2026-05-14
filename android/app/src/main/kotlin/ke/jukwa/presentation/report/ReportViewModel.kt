package ke.jukwa.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.jukwa.domain.incident.SubmitIncidentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitIncidentUseCase: SubmitIncidentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateSeverity(severity: Int) {
        _uiState.value = _uiState.value.copy(severity = severity)
    }

    fun updateAnonymityMode(mode: String) {
        _uiState.value = _uiState.value.copy(anonymityMode = mode)
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(latitude = latitude, longitude = longitude)
    }

    fun submitReport() {
        val state = _uiState.value
        if (state.category.isBlank()) {
            _uiState.value = state.copy(error = "Please select a category")
            return
        }
        if (state.description.isBlank()) {
            _uiState.value = state.copy(error = "Please add a description")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

            submitIncidentUseCase(
                category = state.category,
                severity = state.severity,
                latitude = state.latitude,
                longitude = state.longitude,
                description = state.description,
                anonymityMode = state.anonymityMode,
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isSubmitted = true,
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to submit report",
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState()
    }
}
