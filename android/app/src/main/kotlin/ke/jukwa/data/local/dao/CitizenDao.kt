package ke.jukwa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ke.jukwa.data.local.entity.CitizenEntity

@Dao
interface CitizenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(citizen: CitizenEntity): Long

    @Query("SELECT * FROM citizens WHERE citizenId = :id")
    suspend fun getCitizenById(id: String): CitizenEntity?

    @Query("SELECT * FROM citizens WHERE deviceTokenHash = :hash")
    suspend fun getCitizenByTokenHash(hash: String): CitizenEntity?

    @Query("UPDATE citizens SET anonymityPreference = :preference WHERE citizenId = :id")
    suspend fun updateAnonymityPreference(id: String, preference: String)

    @Query("UPDATE citizens SET gamificationPoints = gamificationPoints + :points WHERE citizenId = :id")
    suspend fun addPoints(id: String, points: Int)
}
