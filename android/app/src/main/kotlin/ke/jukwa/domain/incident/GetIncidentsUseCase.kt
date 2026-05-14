
package ke.jukwa.domain.incident

import ke.jukwa.data.repository.IncidentRepository
import javax.inject.Inject

class GetIncidentsUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository
) {
    operator fun invoke() = incidentRepository.getIncidents()
}
