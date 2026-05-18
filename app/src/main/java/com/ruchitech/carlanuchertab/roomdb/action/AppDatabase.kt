package com.ruchitech.carlanuchertab.roomdb.action
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ruchitech.carlanuchertab.music.MusicDao
import com.ruchitech.carlanuchertab.music.MusicSettingsEntity
import com.ruchitech.carlanuchertab.music.MusicTrackEntity
import com.ruchitech.carlanuchertab.music.PlaylistEntity
import com.ruchitech.carlanuchertab.music.PlaylistTrackEntity
import com.ruchitech.carlanuchertab.roomdb.dao.DashboardDao
import com.ruchitech.carlanuchertab.roomdb.data.Dashboard
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

/*
@Database(entities = [Dashboard::class,FuelLog::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_launcher_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
*/
@Database(
    entities = [
        Dashboard::class,
        FuelLog::class,
        MusicSettingsEntity::class,
        MusicTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
    abstract fun musicDao(): MusicDao
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `music_settings` (
                `id` INTEGER NOT NULL,
                `folderUri` TEXT,
                `folderName` TEXT,
                `lastScanTimestamp` INTEGER NOT NULL,
                `scanStatus` TEXT NOT NULL,
                `errorMessage` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `music_tracks` (
                `uri` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `artist` TEXT NOT NULL,
                `album` TEXT NOT NULL,
                `genre` TEXT NOT NULL,
                `durationMs` INTEGER NOT NULL,
                `trackNumber` INTEGER NOT NULL,
                `discNumber` INTEGER NOT NULL,
                `artworkPath` TEXT,
                `mimeType` TEXT,
                `fileSizeBytes` INTEGER NOT NULL,
                `lastModified` INTEGER NOT NULL,
                `isAvailable` INTEGER NOT NULL,
                PRIMARY KEY(`uri`)
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_music_tracks_title` ON `music_tracks` (`title`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_music_tracks_artist` ON `music_tracks` (`artist`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_music_tracks_album` ON `music_tracks` (`album`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_music_tracks_genre` ON `music_tracks` (`genre`)"
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlists_name` ON `playlists` (`name`)"
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_tracks` (
                `playlistId` INTEGER NOT NULL,
                `trackUri` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                PRIMARY KEY(`playlistId`, `trackUri`),
                FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`trackUri`) REFERENCES `music_tracks`(`uri`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_tracks_playlistId` ON `playlist_tracks` (`playlistId`)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_tracks_trackUri` ON `playlist_tracks` (`trackUri`)"
        )
    }
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "car_launcher_db"
        ).addMigrations(MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideDashboardDao(db: AppDatabase): DashboardDao {
        return db.dashboardDao()
    }

    @Provides
    fun provideMusicDao(db: AppDatabase): MusicDao {
        return db.musicDao()
    }
}

