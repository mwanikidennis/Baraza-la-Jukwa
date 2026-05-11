package ke.jukwa.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateIncidentRequest(
    val reporter_id: String? = null,
    val category: String,
    val severity: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String = "",
    val media_urls: List<String> = emptyList(),
    val anonymity_mode: String = "STANDARD",
)

@Serializable
data class IncidentResponse(
    val incident_id: String? = null,
    val reporter_id: String? = null,
    val incident_category: String? = null,
    val severity_score: Int? = null,
    val location_json: LocationJson? = null,
    val ward_id: Int? = null,
    val description: String? = null,
    val media_urls: List<String>? = null,
    val anonymity_mode: String? = null,
    val status: String? = null,
    val routed_agencies: List<String>? = null,
    val reported_at: String? = null,
    val acknowledged_at: String? = null,
    val resolved_at: String? = null,
)

@Serializable
data class LocationJson(
    val type: String? = null,
    val coordinates: List<Double>? = null,
)

@Serializable
data class IncidentStatusUpdate(
    val status: String,
)

@Serializable
data class RegisterCitizenRequest(
    val device_id: String,
    val ward_id: Int? = null,
)

@Serializable
data class CitizenResponse(
    val citizen_id: String? = null,
    val device_token_hash: String? = null,
    val ward_id: Int? = null,
    val anonymity_preference: String? = null,
    val gamification_points: Int? = null,
    val created_at: String? = null,
)

@Serializable
data class AnonymityPreferenceUpdate(
    val anonymity_preference: String,
)

@Serializable
data class CreateCommitmentRequest(
    val origin_type: String,
    val sector: String,
    val promise_summary: String,
    val affected_ward_id: Int? = null,
    val responsible_agency_id: String? = null,
    val sla_deadline: String? = null,
)

@Serializable
data class CommitmentResponse(
    val commitment_id: String? = null,
    val origin_type: String? = null,
    val sector: String? = null,
    val promise_summary: String? = null,
    val status: String? = null,
    val affected_ward_id: Int? = null,
    val responsible_agency_id: String? = null,
    val created_at: String? = null,
)

@Serializable
data class EmergencySosRequest(
    val citizen_id: String? = null,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val media_stream_url: String? = null,
)

@Serializable
data class EmergencySosResponse(
    val status: String? = null,
    val incident_id: String? = null,
    val tracking_url: String? = null,
)
