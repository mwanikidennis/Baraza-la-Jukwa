package ke.jukwa.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.jukwa.data.local.dao.IncidentDao
import ke.jukwa.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkType(val priority: Int) {
    WIFI(4),
    CELLULAR_4G(3),
    CELLULAR_3G(2),
    CELLULAR_2G(1),
    OFFLINE(0)
}

enum class SyncDecision {
    SYNC_ALL_WITH_MEDIA,
    SYNC_ALL_COMPRESSED,
    SYNC_EMERGENCY_ONLY,
    QUEUE_LOCALLY
}

@Singleton
class NetworkAwareSyncStrategy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val incidentDao: IncidentDao,
    private val incidentRepository: IncidentRepository
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _currentNetwork = MutableStateFlow(detectNetworkType())
    val currentNetwork: StateFlow<NetworkType> = _currentNetwork.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    enum class SyncStatus {
        IDLE, SYNCING, SYNCED, ERROR, WAITING_FOR_NETWORK
    }

    fun detectNetworkType(): NetworkType {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.OFFLINE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.OFFLINE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val downSpeed = capabilities.linkDownstreamBandwidthKbps
                when {
                    downSpeed >= 5000 -> NetworkType.CELLULAR_4G
                    downSpeed >= 300 -> NetworkType.CELLULAR_3G
                    else -> NetworkType.CELLULAR_2G
                }
            }
            else -> NetworkType.OFFLINE
        }
    }

    fun getSyncDecision(networkType: NetworkType): SyncDecision {
        return when (networkType) {
            NetworkType.WIFI -> SyncDecision.SYNC_ALL_WITH_MEDIA
            NetworkType.CELLULAR_4G -> SyncDecision.SYNC_ALL_COMPRESSED
            NetworkType.CELLULAR_3G -> SyncDecision.SYNC_EMERGENCY_ONLY
            NetworkType.CELLULAR_2G -> SyncDecision.SYNC_EMERGENCY_ONLY
            NetworkType.OFFLINE -> SyncDecision.QUEUE_LOCALLY
        }
    }

    suspend fun executeSync() {
        val networkType = detectNetworkType()
        _currentNetwork.value = networkType
        val decision = getSyncDecision(networkType)

        if (decision == SyncDecision.QUEUE_LOCALLY) {
            _syncStatus.value = SyncStatus.WAITING_FOR_NETWORK
            return
        }

        _syncStatus.value = SyncStatus.SYNCING

        try {
            val unsyncedIncidents = when (decision) {
                SyncDecision.SYNC_ALL_WITH_MEDIA,
                SyncDecision.SYNC_ALL_COMPRESSED -> {
                    incidentDao.getUnsyncedIncidents()
                }
                SyncDecision.SYNC_EMERGENCY_ONLY -> {
                    incidentDao.getUnsyncedIncidentsByPriority(maxPriority = 1)
                }
                SyncDecision.QUEUE_LOCALLY -> emptyList()
            }

            for (incident in unsyncedIncidents) {
                try {
                    incidentRepository.syncIncidentToServer(incident)
                    incidentDao.markSynced(incident.incidentId)
                } catch (_: Exception) {
                    // Individual failure doesn't stop the batch
                }
            }

            _syncStatus.value = SyncStatus.SYNCED
        } catch (_: Exception) {
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _currentNetwork.value = detectNetworkType()
            }

            override fun onLost(network: Network) {
                _currentNetwork.value = NetworkType.OFFLINE
                _syncStatus.value = SyncStatus.WAITING_FOR_NETWORK
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _currentNetwork.value = detectNetworkType()
            }
        })
    }
}
