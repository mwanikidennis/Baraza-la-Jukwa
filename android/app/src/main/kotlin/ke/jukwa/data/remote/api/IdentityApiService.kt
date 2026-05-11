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
import ke.jukwa.data.remote.dto.AnonymityPreferenceUpdate
import ke.jukwa.data.remote.dto.CitizenResponse
import ke.jukwa.data.remote.dto.RegisterCitizenRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityApiService @Inject constructor(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun registerCitizen(request: RegisterCitizenRequest): CitizenResponse {
        return client.post {
            url("$baseUrl/register")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getCitizen(citizenId: String): CitizenResponse {
        return client.get {
            url("$baseUrl/me/$citizenId")
        }.body()
    }

    suspend fun updateAnonymityPreference(citizenId: String, preference: String): CitizenResponse {
        return client.patch {
            url("$baseUrl/me/$citizenId/preferences")
            contentType(ContentType.Application.Json)
            setBody(AnonymityPreferenceUpdate(preference))
        }.body()
    }
}
