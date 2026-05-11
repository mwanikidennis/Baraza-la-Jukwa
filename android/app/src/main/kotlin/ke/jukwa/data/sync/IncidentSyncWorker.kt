package ke.jukwa.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.jukwa.data.repository.IncidentRepository

@HiltWorker
class IncidentSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: IncidentRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsynced = repository.getUnsyncedIncidents()
            var allSucceeded = true

            for (incident in unsynced) {
                val result = repository.syncIncidentToServer(incident)
                if (result.isFailure) {
                    allSucceeded = false
                }
            }

            if (allSucceeded) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
