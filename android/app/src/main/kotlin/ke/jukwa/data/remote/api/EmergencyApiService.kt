package ke.jukwa.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ke.jukwa.data.remote.dto.EmergencySosRequest
import ke.jukwa.data.remote.dto.EmergencySosResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyApiService @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun sendSos(request: EmergencySosRequest): EmergencySosResponse {
        return client.post {
            url("$baseUrl/sos")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
