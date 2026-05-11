package ke.jukwa.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ke.jukwa.data.local.entity.IncidentEntity

@Dao
interface IncidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incident: IncidentEntity): Long

    @Update
    suspend fun update(incident: IncidentEntity)

    @Delete
    suspend fun delete(incident: IncidentEntity)

    @Query("SELECT * FROM incidents ORDER BY reportedAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE incidentId = :id")
    suspend fun getIncidentById(id: String): IncidentEntity?

    @Query("SELECT * FROM incidents WHERE isSynced = 0 ORDER BY syncPriority ASC, reportedAt ASC")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    @Query("SELECT * FROM incidents WHERE isSynced = 0 AND syncPriority <= :maxPriority ORDER BY syncPriority ASC, reportedAt ASC")
    suspend fun getUnsyncedIncidentsByPriority(maxPriority: Int): List<IncidentEntity>

    @Query("UPDATE incidents SET isSynced = 1, status = :status WHERE incidentId = :id")
    suspend fun markSynced(id: String, status: String = "SUBMITTED")

    @Query("SELECT COUNT(*) FROM incidents WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("SELECT * FROM incidents WHERE status = :status ORDER BY reportedAt DESC")
    fun getIncidentsByStatus(status: String): Flow<List<IncidentEntity>>

    @Query("DELETE FROM incidents WHERE isSynced = 1 AND reportedAt < :cutoffTimestamp")
    suspend fun deleteOldSyncedRecords(cutoffTimestamp: Long): Int
}
