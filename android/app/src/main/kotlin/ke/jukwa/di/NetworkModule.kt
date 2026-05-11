package ke.jukwa.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ke.jukwa.data.remote.api.EmergencyApiService
import ke.jukwa.data.remote.api.IdentityApiService
import ke.jukwa.data.remote.api.IncidentApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:3001"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideKtorClient(json: Json): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    @Provides
    @Singleton
    fun provideIncidentApiService(client: HttpClient): IncidentApiService {
        return IncidentApiService(client, BASE_URL)
    }

    @Provides
    @Singleton
    fun provideIdentityApiService(client: HttpClient): IdentityApiService {
        return IdentityApiService(client, "http://10.0.2.2:3006")
    }

    @Provides
    @Singleton
    fun provideEmergencyApiService(client: HttpClient): EmergencyApiService {
        return EmergencyApiService(client, "http://10.0.2.2:3004")
    }
}
