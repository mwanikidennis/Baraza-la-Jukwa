package ke.jukwa.presentation.report

data class ReportUiState(
    val category: String = "",
    val severity: Int = 1,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val anonymityMode: String = "STANDARD"
)
