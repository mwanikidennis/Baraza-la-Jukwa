package ke.jukwa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ke.jukwa.data.local.dao.CitizenDao
import ke.jukwa.data.local.dao.CommitmentDao
import ke.jukwa.data.local.dao.IncidentDao
import ke.jukwa.data.local.entity.CitizenEntity
import ke.jukwa.data.local.entity.CommitmentEntity
import ke.jukwa.data.local.entity.IncidentEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        IncidentEntity::class,
        CitizenEntity::class,
        CommitmentEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class JukwaDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun citizenDao(): CitizenDao
    abstract fun commitmentDao(): CommitmentDao
}
