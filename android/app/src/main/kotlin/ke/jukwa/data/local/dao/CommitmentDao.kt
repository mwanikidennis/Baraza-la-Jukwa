package ke.jukwa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ke.jukwa.data.local.entity.CommitmentEntity

@Dao
interface CommitmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commitment: CommitmentEntity): Long

    @Query("SELECT * FROM commitments ORDER BY createdAt DESC")
    fun getAllCommitments(): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE affectedWardId = :wardId ORDER BY createdAt DESC")
    fun getCommitmentsByWard(wardId: Int): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE commitmentId = :id")
    suspend fun getCommitmentById(id: String): CommitmentEntity?

    @Query("UPDATE commitments SET status = :status, updatedAt = :timestamp WHERE commitmentId = :id")
    suspend fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())
}
