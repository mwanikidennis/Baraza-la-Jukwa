package ke.jukwa.data.repository

import ke.jukwa.data.local.dao.IncidentDao
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.data.remote.api.IncidentApiService
import ke.jukwa.data.remote.dto.CreateIncidentRequest
import ke.jukwa.data.remote.dto.IncidentResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val incidentDao: IncidentDao,
    private val incidentApiService: IncidentApiService,
) {
    fun getIncidents() = incidentDao.getAllIncidents()

    suspend fun getIncidentById(id: String) = incidentDao.getIncidentById(id)

    suspend fun getUnsyncedIncidents() = incidentDao.getUnsyncedIncidents()

    suspend fun getUnsyncedIncidentsByPriority(maxPriority: Int) =
        incidentDao.getUnsyncedIncidentsByPriority(maxPriority)

    suspend fun saveIncidentLocally(incident: IncidentEntity): Long {
        return incidentDao.insert(incident)
    }

    suspend fun updateIncident(incident: IncidentEntity) {
        incidentDao.update(incident)
    }

    suspend fun markSynced(id: String, status: String = "SUBMITTED") {
        incidentDao.markSynced(id, status)
    }

    suspend fun syncIncidentToServer(incident: IncidentEntity): Result<IncidentResponse> {
        return try {
            val request = CreateIncidentRequest(
                reporter_id = incident.reporterId,
                category = incident.incidentCategory,
                severity = incident.severityScore,
                latitude = incident.latitude,
                longitude = incident.longitude,
                description = incident.description,
                media_urls = if (incident.mediaUrls.isNotBlank())
                    incident.mediaUrls.split(",").map { it.trim() } else emptyList(),
                anonymity_mode = incident.anonymityMode,
            )
            val response = incidentApiService.createIncident(request)
            if (response.incident_id != null) {
                markSynced(incident.incidentId, response.status ?: "SUBMITTED")
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteIncidents(limit: Int = 20, offset: Int = 0): Result<List<IncidentResponse>> {
        return try {
            val incidents = incidentApiService.getIncidents(limit, offset)
            Result.success(incidents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount() = incidentDao.getUnsyncedCount()
}
