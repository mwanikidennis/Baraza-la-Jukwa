package ke.jukwa.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ke.jukwa.data.remote.dto.CreateIncidentRequest
import ke.jukwa.data.remote.dto.IncidentResponse
import ke.jukwa.data.remote.dto.IncidentStatusUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentApiService @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun createIncident(request: CreateIncidentRequest): IncidentResponse {
        return client.post {
            url("$baseUrl/incidents")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getIncidents(limit: Int = 20, offset: Int = 0): List<IncidentResponse> {
        return client.get {
            url("$baseUrl/incidents?limit=$limit&offset=$offset")
        }.body()
    }

    suspend fun getIncidentById(id: String): IncidentResponse {
        return client.get {
            url("$baseUrl/incidents/$id")
        }.body()
    }

    suspend fun updateIncidentStatus(id: String, status: String): IncidentResponse {
        return client.patch {
            url("$baseUrl/incidents/$id/status")
            contentType(ContentType.Application.Json)
            setBody(IncidentStatusUpdate(status))
        }.body()
    }

    suspend fun getIncidentsNear(lat: Double, lon: Double, radiusMeters: Int = 5000): List<IncidentResponse> {
        return client.get {
            url("$baseUrl/incidents/near?lat=$lat&lon=$lon&radius=$radiusMeters")
        }.body()
    }

    suspend fun getIncidentsByWard(wardId: Int): List<IncidentResponse> {
        return client.get {
            url("$baseUrl/incidents/ward/$wardId")
        }.body()
    }
}
