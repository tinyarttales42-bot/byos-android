package app.byos.android.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.byos.android.ByosApplication
import app.byos.android.data.*
import app.byos.android.model.*
import app.byos.android.playback.PlaybackConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryState(val songs: List<Song> = emptyList(), val demo: Boolean = false, val scanning: Boolean = false,
    val message: String? = null, val playlists: List<PlaylistEntity> = emptyList(),
    val playlistSongs: List<PlaylistSongEntity> = emptyList(), val tags: List<TagEntity> = emptyList())

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as ByosApplication).repository
    private val base = MutableStateFlow(LibraryState())
    private val rawSongs = MutableStateFlow<List<Song>>(emptyList())
    val playback = PlaybackConnection(application)
    private val annotated = combine(rawSongs, repository.dao.ratings(), repository.dao.tags(), repository.dao.songTags()) { songs, ratings, tags, links ->
        val stars = ratings.associate { it.songId to it.stars }
        val names = tags.associate { it.id to it.name }
        val assignments = links.groupBy { it.songId }
        songs.map { song -> song.copy(rating = stars[song.id] ?: song.rating,
            tags = song.tags + assignments[song.id].orEmpty().mapNotNull { names[it.tagId] }) }
    }
    val state = combine(base, annotated, repository.dao.playlists(), repository.dao.playlistSongs(), repository.dao.tags()) { ui, songs, playlists, links, tags ->
        ui.copy(songs = songs, playlists = playlists, playlistSongs = links, tags = tags)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryState())

    fun preview() { rawSongs.value = DemoLibrary.songs; base.update { it.copy(demo = true, message = null) } }
    fun scan() {
        if (base.value.scanning) return
        viewModelScope.launch {
            base.update { it.copy(scanning = true, demo = false, message = null) }
            rawSongs.value = emptyList()
            try { rawSongs.value = repository.scan() }
            catch (e: CancellationException) { throw e }
            catch (_: SecurityException) { message("Allow audio access to scan your device.") }
            catch (_: Exception) { message("Couldn't read the media index. Please try again.") }
            finally { base.update { it.copy(scanning = false) } }
        }
    }
    fun permissionDenied() { rawSongs.value = emptyList(); base.update { it.copy(demo = false, message = "Audio access is off. Allow it in Settings, or explore the sample library.") } }
    fun message(value: String?) { base.update { it.copy(message = value) } }
    private fun mutate(block: suspend () -> Unit) { viewModelScope.launch {
        try { block() }
        catch (e: CancellationException) { throw e }
        catch (_: SQLiteConstraintException) { message("That name already exists. Choose another name.") }
        catch (e: IllegalArgumentException) { message(e.message ?: "Check the entered value.") }
        catch (e: IllegalStateException) { message(e.message ?: "Couldn't save this change.") }
        catch (_: Exception) { message("Couldn't save this change. Please try again.") }
    } }
    fun createPlaylist(name: String) = mutate { repository.createPlaylist(name) }
    fun renamePlaylist(id: Long, name: String) = mutate { repository.renamePlaylist(id, name) }
    fun deletePlaylist(id: Long) = mutate { repository.dao.deletePlaylist(id) }
    fun addSong(id: Long, song: Song) = mutate {
        require(song.uri != null) { "Scan device audio before adding songs to playlists." }
        repository.dao.addSong(PlaylistSongEntity(id, song.id))
    }
    fun removeSong(id: Long, song: Song) = mutate { repository.dao.removeSong(id, song.id) }
    fun createTag(name: String) = mutate { repository.createTag(name) }
    fun tag(song: Song, name: String) = mutate {
        if (song.uri == null) message("Sample songs are for preview. Scan your device to save tags.")
        else repository.assignTag(song.id, name)
    }
    fun rate(song: Song, stars: Int) = mutate {
        if (song.uri == null) rawSongs.update { songs -> songs.map { if (it.id == song.id) it.copy(rating = validRating(stars)) else it } }
        else repository.rate(song.id, stars)
    }
    override fun onCleared() { playback.close(); super.onCleared() }
}
