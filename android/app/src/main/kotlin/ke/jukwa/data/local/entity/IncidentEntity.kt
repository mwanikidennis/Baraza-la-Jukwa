package ke.jukwa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey
    val incidentId: String = UUID.randomUUID().toString(),
    val reporterId: String? = null,
    val incidentCategory: String,
    val severityScore: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val wardId: Int? = null,
    val description: String = "",
    val mediaUrls: String = "",
    val anonymityMode: String = "STANDARD",
    val status: String = "QUEUED_FOR_SYNC",
    val routedAgencies: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val resolvedAt: Long? = null,
    val syncPriority: Int = 4,
    val isSynced: Boolean = false,
)
