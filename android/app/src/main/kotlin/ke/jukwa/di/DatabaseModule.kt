package ke.jukwa.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ke.jukwa.data.local.JukwaDatabase
import ke.jukwa.data.local.dao.CitizenDao
import ke.jukwa.data.local.dao.CommitmentDao
import ke.jukwa.data.local.dao.IncidentDao
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Future schema migrations go here
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JukwaDatabase {
        return Room.databaseBuilder(
            context,
            JukwaDatabase::class.java,
            "jukwa.db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideIncidentDao(db: JukwaDatabase): IncidentDao = db.incidentDao()

    @Provides
    fun provideCitizenDao(db: JukwaDatabase): CitizenDao = db.citizenDao()

    @Provides
    fun provideCommitmentDao(db: JukwaDatabase): CommitmentDao = db.commitmentDao()
}
