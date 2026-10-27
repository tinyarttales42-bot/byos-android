package app.byos.android

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.byos.android.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryDatabaseTest {
    private lateinit var database: LibraryDatabase
    private lateinit var dao: LibraryDao
    @Before fun setup() { database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LibraryDatabase::class.java).build(); dao = database.libraryDao() }
    @After fun close() { database.close() }
    @Test fun duplicateTagsAndAssignmentsAreIgnored() = runTest {
        val tag = dao.insertTag(TagEntity(name = "Chill", nameKey = "chill"))
        assertEquals(-1L, dao.insertTag(TagEntity(name = "CHILL", nameKey = "chill")))
        dao.assignTag(SongTagEntity("content://audio/1", tag))
        dao.assignTag(SongTagEntity("content://audio/1", tag))
        assertEquals(1, dao.tags().first().size)
        assertEquals(1, dao.songTags().first().size)
    }
    @Test fun deletingPlaylistCascadesMembershipAndPreservesRating() = runTest {
        val id = dao.insertPlaylist(PlaylistEntity(name = "Night", nameKey = "night"))
        dao.addSong(PlaylistSongEntity(id, "song-1"))
        dao.addSong(PlaylistSongEntity(id, "song-1"))
        assertEquals(1, dao.playlistSongs().first().size)
        dao.rate(RatingEntity("song-1", 4))
        dao.renamePlaylist(id, "Day", "day")
        assertEquals("Day", dao.playlists().first().single().name)
        dao.deletePlaylist(id)
        assertTrue(dao.playlistSongs().first().isEmpty())
        assertEquals(4, dao.ratings().first().single().stars)
    }
    @Test fun ratingUpsertReplacesPreviousValue() = runTest {
        dao.rate(RatingEntity("song-1", 2)); dao.rate(RatingEntity("song-1", 5))
        assertEquals(1, dao.ratings().first().size)
        assertEquals(5, dao.ratings().first().single().stars)
    }
}
