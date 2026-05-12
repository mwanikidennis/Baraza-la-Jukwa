package ke.jukwa.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class IncidentSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val networkAwareSyncStrategy: NetworkAwareSyncStrategy,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val decision = networkAwareSyncStrategy.getSyncDecision(networkAwareSyncStrategy.detectNetworkType())

        if (decision == SyncDecision.QUEUE_LOCALLY) {
            return Result.failure()
        }

        return try {
            networkAwareSyncStrategy.executeSync()
            when (networkAwareSyncStrategy.syncStatus.value) {
                NetworkAwareSyncStrategy.SyncStatus.SYNCED -> Result.success()
                NetworkAwareSyncStrategy.SyncStatus.ERROR -> Result.retry()
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
