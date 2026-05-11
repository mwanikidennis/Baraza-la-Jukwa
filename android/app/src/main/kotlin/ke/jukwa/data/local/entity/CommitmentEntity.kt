package ke.jukwa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey
    val commitmentId: String,
    val originType: String,
    val sector: String,
    val promiseSummary: String,
    val fulfillmentCriteria: String = "",
    val affectedWardId: Int? = null,
    val affectedFacilityName: String = "",
    val responsibleAgencyId: String? = null,
    val responsibleOfficialName: String = "",
    val status: String = "CAPTURED",
    val slaDeadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
