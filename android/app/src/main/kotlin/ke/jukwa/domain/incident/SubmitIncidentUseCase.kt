package ke.jukwa.domain.incident

import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.data.repository.IncidentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmitIncidentUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(
        category: String,
        severity: Int,
        latitude: Double,
        longitude: Double,
        description: String,
        mediaUrls: String = "",
        anonymityMode: String = "STANDARD",
        reporterId: String? = null,
        wardId: Int? = null,
    ): Result<IncidentEntity> {
        val syncPriority = when (category) {
            "medical_emergency", "fire", "accident" -> 0
            "robbery", "assault", "gang_activity", "drug_trafficking",
            "domestic_violence", "stalking", "suspicious_activity", "theft" -> 1
            "pothole", "congestion", "public_transport_delay", "road_closure",
            "unsafe_driving", "matatu_violation" -> 2
            else -> 3
        }

        val incident = IncidentEntity(
            reporterId = reporterId,
            incidentCategory = category,
            severityScore = severity.coerceIn(1, 5),
            latitude = latitude,
            longitude = longitude,
            wardId = wardId,
            description = description,
            mediaUrls = mediaUrls,
            anonymityMode = anonymityMode,
            status = "QUEUED_FOR_SYNC",
            syncPriority = syncPriority,
            isSynced = false,
        )

        val rowId = repository.saveIncidentLocally(incident)
        return if (rowId > 0) {
            Result.success(incident)
        } else {
            Result.failure(Exception("Failed to save incident locally"))
        }
    }
}
