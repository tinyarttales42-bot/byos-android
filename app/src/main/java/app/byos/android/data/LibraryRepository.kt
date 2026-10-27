package app.byos.android.data

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import app.byos.android.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(private val resolver: ContentResolver, val dao: LibraryDao) {
    suspend fun scan(): List<Song> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION)
        buildList {
            resolver.query(collection, projection, "${MediaStore.Audio.Media.DURATION} > 0", null,
                "${MediaStore.Audio.Media.TITLE} ASC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(collection, cursor.getLong(0)).toString()
                    add(Song(uri, cursor.getString(1).orEmpty().ifBlank { "Untitled" },
                        cursor.getString(2).orEmpty().takeUnless { it == "<unknown>" || it.isBlank() } ?: "Unknown artist",
                        cursor.getString(3).orEmpty().ifBlank { "Unknown album" }, cursor.getLong(4), uri))
                }
            } ?: error("The device media index is unavailable. Try scanning again.")
        }
    }

    suspend fun createPlaylist(name: String) {
        val clean = checkedName(name)
        check(dao.insertPlaylist(PlaylistEntity(name = clean, nameKey = labelKey(clean))) != -1L) { "A playlist with this name already exists." }
    }
    suspend fun renamePlaylist(id: Long, name: String) {
        val clean = checkedName(name)
        dao.renamePlaylist(id, clean, labelKey(clean))
    }
    suspend fun createTag(name: String): Long {
        val clean = checkedName(name)
        val key = labelKey(clean)
        dao.insertTag(TagEntity(name = clean, nameKey = key))
        return checkNotNull(dao.tagId(key))
    }
    suspend fun assignTag(songId: String, name: String) = dao.assignTag(SongTagEntity(songId, createTag(name)))
    suspend fun rate(songId: String, stars: Int) = dao.rate(RatingEntity(songId, validRating(stars)))

    private fun checkedName(value: String): String = normalizeLabel(value).also {
        require(it.isNotEmpty()) { "Enter a name." }
        require(it.length <= 60) { "Use 60 characters or fewer." }
    }
}
