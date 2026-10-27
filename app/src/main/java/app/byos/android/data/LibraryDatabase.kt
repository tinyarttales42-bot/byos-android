package app.byos.android.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists", indices = [Index(value = ["nameKey"], unique = true)])
data class PlaylistEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val nameKey: String)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"],
    foreignKeys = [ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE)])
data class PlaylistSongEntity(val playlistId: Long, val songId: String)

@Entity(tableName = "tags", indices = [Index(value = ["nameKey"], unique = true)])
data class TagEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val nameKey: String)

@Entity(tableName = "song_tags", primaryKeys = ["songId", "tagId"], indices = [Index("tagId")],
    foreignKeys = [ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)])
data class SongTagEntity(val songId: String, val tagId: Long)

@Entity(tableName = "ratings")
data class RatingEntity(@PrimaryKey val songId: String, val stars: Int)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM playlists ORDER BY nameKey") fun playlists(): Flow<List<PlaylistEntity>>
    @Query("SELECT * FROM playlist_songs") fun playlistSongs(): Flow<List<PlaylistSongEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertPlaylist(value: PlaylistEntity): Long
    @Query("UPDATE playlists SET name = :name, nameKey = :key WHERE id = :id") suspend fun renamePlaylist(id: Long, name: String, key: String)
    @Query("DELETE FROM playlists WHERE id = :id") suspend fun deletePlaylist(id: Long)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addSong(value: PlaylistSongEntity)
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId") suspend fun removeSong(playlistId: Long, songId: String)
    @Query("SELECT * FROM tags ORDER BY nameKey") fun tags(): Flow<List<TagEntity>>
    @Query("SELECT * FROM song_tags") fun songTags(): Flow<List<SongTagEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTag(value: TagEntity): Long
    @Query("SELECT id FROM tags WHERE nameKey = :key") suspend fun tagId(key: String): Long?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun assignTag(value: SongTagEntity)
    @Query("DELETE FROM song_tags WHERE songId = :songId AND tagId = :tagId") suspend fun unassignTag(songId: String, tagId: Long)
    @Query("SELECT * FROM ratings") fun ratings(): Flow<List<RatingEntity>>
    @Upsert suspend fun rate(value: RatingEntity)
}

@Database(entities = [PlaylistEntity::class, PlaylistSongEntity::class, TagEntity::class, SongTagEntity::class, RatingEntity::class], version = 1, exportSchema = true)
abstract class LibraryDatabase : RoomDatabase() { abstract fun libraryDao(): LibraryDao }
