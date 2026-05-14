package ke.jukwa.presentation.myreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.jukwa.data.local.entity.IncidentEntity
import ke.jukwa.data.repository.IncidentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyReportsViewModel @Inject constructor(
    repository: IncidentRepository,
) : ViewModel() {

    val incidents: StateFlow<List<IncidentEntity>> = repository.getIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
