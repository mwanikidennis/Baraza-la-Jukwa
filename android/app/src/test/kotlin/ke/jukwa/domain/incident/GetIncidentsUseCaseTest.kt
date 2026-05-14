
package ke.jukwa.domain.incident

import app.cash.turbine.test
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.data.repository.IncidentRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetIncidentsUseCaseTest {

    private lateinit var getIncidentsUseCase: GetIncidentsUseCase
    private val incidentRepository: IncidentRepository = mock()

    @BeforeEach
    fun setUp() {
        getIncidentsUseCase = GetIncidentsUseCase(incidentRepository)
    }

    @Test
    fun `invoke should return a flow of incidents`() = runTest {
        // Given
        val incidents = listOf(
            IncidentEntity(
                incidentId = "1",
                reporterId = "reporter1",
                incidentCategory = "Theft",
                severityScore = 5,
                latitude = 1.0,
                longitude = 1.0,
                description = "Description 1",
                mediaUrls = "",
                anonymityMode = "STANDARD",
                isSynced = false,
                status = "PENDING"
            ),
            IncidentEntity(
                incidentId = "2",
                reporterId = "reporter2",
                incidentCategory = "Vandalism",
                severityScore = 3,
                latitude = 2.0,
                longitude = 2.0,
                description = "Description 2",
                mediaUrls = "",
                anonymityMode = "INCOGNITO",
                isSynced = true,
                status = "SUBMITTED"
            )
        )
        whenever(incidentRepository.getIncidents()).thenReturn(flowOf(incidents))

        // When
        val result = getIncidentsUseCase()

        // Then
        result.test { 
            assertEquals(incidents, awaitItem())
            awaitComplete()
        }
    }
}
