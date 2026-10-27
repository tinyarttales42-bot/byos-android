package app.byos.android.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.*
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.byos.android.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaybackState(val songId: String? = null, val playing: Boolean = false, val shuffle: Boolean = false,
    val repeat: Int = Player.REPEAT_MODE_OFF, val positionMs: Long = 0, val durationMs: Long = 0, val error: String? = null)

class PlaybackConnection(context: Context) {
    private val mutable = MutableStateFlow(PlaybackState())
    val state = mutable.asStateFlow()
    private var controller: MediaController? = null
    private val future = MediaController.Builder(context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java))).buildAsync()
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = refresh()
        override fun onPlayerError(error: PlaybackException) {
            mutable.value = mutable.value.copy(error = "This file cannot be played. It may have moved or use an unsupported format.")
        }
    }
    init {
        future.addListener({
            runCatching { future.get() }.onSuccess { controller = it; it.addListener(listener); refresh() }
                .onFailure { mutable.value = mutable.value.copy(error = "Playback service is unavailable. Reopen BYOS to retry.") }
        }, ContextCompat.getMainExecutor(context))
    }
    fun play(song: Song, queue: List<Song>) {
        val player = controller ?: run { mutable.value = mutable.value.copy(error = "Playback is connecting. Please try again."); return }
        val local = queue.filter { it.uri != null }
        val index = local.indexOfFirst { it.id == song.id }
        if (index < 0) return
        mutable.value = mutable.value.copy(error = null)
        player.setMediaItems(local.map { item -> MediaItem.Builder().setMediaId(item.id).setUri(item.uri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(item.title).setArtist(item.artist).setAlbumTitle(item.album).build()).build() }, index, 0)
        player.prepare()
        player.play()
    }
    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun shuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
    fun repeat() { controller?.let { it.repeatMode = (it.repeatMode + 1) % 3 } }
    fun seek(positionMs: Long) { controller?.seekTo(positionMs) }
    fun refresh() { controller?.let { mutable.value = mutable.value.copy(songId = it.currentMediaItem?.mediaId,
        playing = it.isPlaying, shuffle = it.shuffleModeEnabled, repeat = it.repeatMode,
        positionMs = it.currentPosition.coerceAtLeast(0), durationMs = it.duration.coerceAtLeast(0)) } }
    fun close() { controller?.removeListener(listener); MediaController.releaseFuture(future) }
}
