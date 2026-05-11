package ke.jukwa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citizens")
data class CitizenEntity(
    @PrimaryKey
    val citizenId: String,
    val deviceTokenHash: String,
    val wardId: Int? = null,
    val anonymityPreference: String = "STANDARD",
    val gamificationPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
