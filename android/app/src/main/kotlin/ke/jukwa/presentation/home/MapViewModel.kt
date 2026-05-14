
package ke.jukwa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.data.remote.api.LocationService
import ke.jukwa.domain.incident.GetIncidentsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationService: LocationService,
    private val getIncidentsUseCase: GetIncidentsUseCase
) : ViewModel() {

    private val _location = MutableStateFlow<LocationResult?>(null)
    val location: StateFlow<LocationResult?> = _location

    private val _incidents = MutableStateFlow<List<IncidentEntity>>(emptyList())
    val incidents: StateFlow<List<IncidentEntity>> = _incidents

    init {
        viewModelScope.launch {
            getIncidentsUseCase()
                .catch { e ->
                    // Handle error
                }
                .collect { 
                    _incidents.value = it
                }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            locationService.requestLocationUpdates()
                .catch { e ->
                    // Handle error
                }
                .collect { 
                    _location.value = it
                }
        }
    }
}
