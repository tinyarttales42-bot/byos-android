package app.byos.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import app.byos.android.model.*
import app.byos.android.playback.PlaybackState
import app.byos.android.ui.components.*
import app.byos.android.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ByosApp(state: LibraryState, playback: PlaybackState, vm: LibraryViewModel, onScan: () -> Unit, onSettings: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf("Library") }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(SongSort.TITLE) }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var tagFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var playlistId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var nowPlaying by rememberSaveable { mutableStateOf(false) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var optionsId by remember { mutableStateOf<String?>(null) }
    var nameDialog by remember { mutableStateOf<String?>(null) }
    var renameId by remember { mutableStateOf<Long?>(null) }
    var tagSong by remember { mutableStateOf<Song?>(null) }
    var addSong by remember { mutableStateOf<Song?>(null) }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    val snack = remember { SnackbarHostState() }
    val active = state.songs.find { it.id == (if (state.demo) selectedId else playback.songId) }
    val options = state.songs.find { it.id == optionsId }
    val playlist = state.playlists.find { it.id == playlistId }
    val filtered = state.songs.filtered(query, sort, favorites, tagFilter).filter { song ->
        playlistId == null || state.playlistSongs.any { it.playlistId == playlistId && it.songId == song.id }
    }
    fun openSong(song: Song) {
        selectedId = song.id
        if (!state.demo) vm.playback.play(song, filtered)
        nowPlaying = true
    }
    LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it); vm.message(null) } }
    LaunchedEffect(playback.error) { playback.error?.let { snack.showSnackbar(it) } }
    LaunchedEffect(nowPlaying, playback.playing) { while (nowPlaying && playback.playing) { vm.playback.refresh(); delay(500) } }
    BackHandler(nowPlaying || playlistId != null || tagFilter != null || tab != "Library") {
        when { nowPlaying -> nowPlaying = false; playlistId != null -> playlistId = null; tagFilter != null -> tagFilter = null; else -> tab = "Library" }
    }

    if (showPrivacy) PrivacyDialog(onDismiss = { showPrivacy = false })

    Scaffold(containerColor = Ink, snackbarHost = { SnackbarHost(snack) }, bottomBar = {
        if (!nowPlaying) Column {
            active?.let { song ->
                Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Panel).clickable { nowPlaying = true }.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    RecordArt(Modifier.size(40.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (state.demo) "Sample preview · no audio" else song.artist, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    IconButton(onClick = { vm.playback.toggle() }, enabled = !state.demo) { Icon(if (playback.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (playback.playing) "Pause" else "Play", tint = Lavender) }
                }
            }
            NavigationBar(containerColor = Ink, tonalElevation = 0.dp) {
                listOf("Library" to Icons.Rounded.LibraryMusic, "Playlists" to Icons.AutoMirrored.Rounded.QueueMusic, "Tags" to Icons.Rounded.Sell).forEach { (label, icon) ->
                    NavigationBarItem(selected = tab == label, onClick = { tab = label; playlistId = null; tagFilter = null },
                        icon = { Icon(icon, null) }, label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Panel, selectedIconColor = Lavender, selectedTextColor = Lavender))
                }
            }
        }
    }) { padding ->
        if (nowPlaying && active != null) {
            PlayerScreen(active, state.demo, playback, Modifier.padding(padding), onBack = { nowPlaying = false },
                onMore = { optionsId = active.id }, onRate = { vm.rate(active, it) }, onToggle = vm.playback::toggle,
                onNext = vm.playback::next, onPrevious = vm.playback::previous, onShuffle = vm.playback::shuffle,
                onRepeat = vm.playback::repeat, onSeek = vm.playback::seek)
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("B Y O S", color = Lavender, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Text("Bring your own song.", color = Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { showPrivacy = true }) { Icon(Icons.Rounded.PrivacyTip, "Privacy policy", tint = Lavender) }
                        IconButton(onClick = onScan, enabled = !state.scanning) { Icon(Icons.Rounded.Refresh, "Scan device audio", tint = Lavender) }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (playlistId != null || tagFilter != null) IconButton(onClick = { playlistId = null; tagFilter = null }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to library") }
                        Text(playlist?.name ?: tagFilter ?: when (tab) { "Library" -> "Your music.\nYour universe."; "Playlists" -> "Made for your mood."; else -> "A little more you." }, style = MaterialTheme.typography.headlineLarge)
                    }
                }
                if (tab == "Library" || playlistId != null || tagFilter != null) {
                    if (playlistId == null && tagFilter == null) item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("ON YOUR DEVICE", style = MaterialTheme.typography.labelSmall, color = Lavender)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Keep it personal.", style = MaterialTheme.typography.titleLarge)
                                    Text("All your favorites.\nAlways with you.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                                }
                                RecordArt(Modifier.size(88.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.OfflineBolt, null, tint = Blue, modifier = Modifier.size(16.dp))
                                Text("  Offline by design", color = Blue, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.weight(1f))
                                Text(if (state.demo) "SAMPLE LIBRARY" else "${state.songs.size} songs", color = Muted, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (state.songs.isNotEmpty()) {
                        item {
                            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Find a song, artist or tag") },
                                leadingIcon = { Icon(Icons.Rounded.Search, null) }, trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear search") } }, singleLine = true, shape = RoundedCornerShape(18.dp))
                        }
                        item {
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = !favorites, onClick = { favorites = false }, label = { Text("All songs") })
                                FilterChip(selected = favorites, onClick = { favorites = true }, label = { Text("Favorites") }, leadingIcon = { Icon(Icons.Rounded.Star, null, Modifier.size(16.dp)) })
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    AssistChip(onClick = { expanded = true }, label = { Text("Sort: ${sort.name.lowercase().replaceFirstChar { it.uppercase() }}") }, leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Sort, null, Modifier.size(16.dp)) })
                                    DropdownMenu(expanded, onDismissRequest = { expanded = false }) { SongSort.entries.forEach { value -> DropdownMenuItem(text = { Text(value.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { sort = value; expanded = false }) } }
                                }
                            }
                        }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (playlistId != null) "In this playlist" else "The collection", style = MaterialTheme.typography.titleMedium)
                                Text("${filtered.size} tracks", color = Muted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        itemsIndexed(filtered, key = { _, song -> song.id }) { index, song -> SongRow(song, index, { openSong(song) }, { optionsId = song.id }) }
                        if (filtered.isEmpty()) item { EmptyState("No matching songs", "Try a different search or filter.") }
                        if (state.demo) item { Text("Sample titles and artists are fictional. Scan your device to listen to your music.", color = Muted, style = MaterialTheme.typography.bodySmall) }
                    } else item {
                        EmptyState(if (state.scanning) "Finding your music…" else "Your collection starts here", "Give BYOS access to audio files on this device. Your music stays yours.")
                        Button(onClick = onScan, enabled = !state.scanning, modifier = Modifier.fillMaxWidth()) { Text("Scan my music") }
                        TextButton(onClick = vm::preview, modifier = Modifier.fillMaxWidth()) { Text("Explore sample library") }
                        TextButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Audio permission settings") }
                    }
                } else if (tab == "Playlists") {
                    item { Text("A place for every kind of day.", color = Muted); Spacer(Modifier.height(12.dp)); Button(onClick = { nameDialog = "New playlist" }) { Icon(Icons.Rounded.Add, null); Text(" Create playlist") } }
                    if (state.playlists.isEmpty()) item { EmptyState("Make your first mix", "Create a playlist, then add device songs from their options menu.") }
                    itemsIndexed(state.playlists, key = { _, p -> p.id }) { index, p ->
                        GlassCard(Modifier.fillMaxWidth().clickable { playlistId = p.id }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RecordArt(Modifier.size(58.dp), index)
                                Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(p.name, style = MaterialTheme.typography.titleMedium); Text("${state.playlistSongs.count { it.playlistId == p.id }} songs", color = Muted) }
                                var menu by remember { mutableStateOf(false) }
                                Box { IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreHoriz, "Options for playlist ${p.name}") }
                                    DropdownMenu(menu, { menu = false }) {
                                        DropdownMenuItem(text = { Text("Rename") }, onClick = { renameId = p.id; nameDialog = "Rename playlist"; menu = false })
                                        DropdownMenuItem(text = { Text("Delete") }, onClick = { deleteId = p.id; menu = false })
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item { Text("Organize by feeling. Find it in a moment.", color = Muted); Spacer(Modifier.height(12.dp)); Button(onClick = { nameDialog = "New tag" }) { Icon(Icons.Rounded.Add, null); Text(" Create tag") } }
                    val tags = (state.tags.map { it.name } + state.songs.flatMap { it.tags }).distinctBy(::labelKey).sorted()
                    if (tags.isEmpty()) item { EmptyState("Music, with your meaning", "Add tags like Focus, Road trip or Sunday morning. Names are unique, ignoring case.") }
                    itemsIndexed(tags) { _, tag ->
                        GlassCard(Modifier.fillMaxWidth().clickable { tagFilter = tag }) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Sell, null, tint = Pink)
                                Text(tag, Modifier.weight(1f).padding(start = 16.dp), style = MaterialTheme.typography.titleMedium)
                                Text("${state.songs.count { tag in it.tags }} songs", color = Muted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (options != null) ModalBottomSheet(onDismissRequest = { optionsId = null }, containerColor = Panel) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(options.title, style = MaterialTheme.typography.titleLarge)
            Text(options.artist, color = Muted)
            RatingBar(options.rating) { vm.rate(options, it) }
            TextButton(onClick = { addSong = options; optionsId = null }, enabled = !state.demo) { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null); Text(" Add to playlist") }
            TextButton(onClick = { tagSong = options; nameDialog = "Tag this song"; optionsId = null }, enabled = !state.demo) { Icon(Icons.Rounded.Sell, null); Text(" Add a tag") }
            if (playlistId != null) TextButton(onClick = { vm.removeSong(playlistId!!, options); optionsId = null }) { Text("Remove from playlist") }
            if (state.demo) Text("Sample ratings reset when the app closes. Scan device audio to save your collection.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
    nameDialog?.let { title ->
        NameDialog(title, initial = state.playlists.find { it.id == renameId }?.name.orEmpty(), onDismiss = { nameDialog = null; renameId = null; tagSong = null }) { value ->
            when { renameId != null -> vm.renamePlaylist(renameId!!, value); tagSong != null -> vm.tag(tagSong!!, value); title == "New tag" -> vm.createTag(value); else -> vm.createPlaylist(value) }
            nameDialog = null; renameId = null; tagSong = null
        }
    }
    addSong?.let { song -> AlertDialog(onDismissRequest = { addSong = null }, title = { Text("Add to playlist") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (state.playlists.isEmpty()) Text("Create a playlist in the Playlists tab first.")
            state.playlists.forEach { p -> TextButton(onClick = { vm.addSong(p.id, song); addSong = null }) { Text(p.name) } }
        }
    }, confirmButton = { TextButton(onClick = { addSong = null }) { Text("Done") } }) }
    deleteId?.let { id -> AlertDialog(onDismissRequest = { deleteId = null }, title = { Text("Delete playlist?") }, text = { Text("The playlist will be removed. Your audio files stay on your device.") },
        confirmButton = { TextButton(onClick = { vm.deletePlaylist(id); deleteId = null }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } }) }
}

@Composable private fun EmptyState(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.MusicNote, null, Modifier.size(40.dp), tint = Lavender)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        OutlinedTextField(value, { value = it.take(60) }, label = { Text("Name") }, singleLine = true, supportingText = { Text("Up to 60 characters") })
    }, confirmButton = { TextButton(onClick = { onSave(value) }, enabled = value.isNotBlank()) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun PlayerScreen(song: Song, demo: Boolean, state: PlaybackState, modifier: Modifier,
    onBack: () -> Unit, onMore: () -> Unit, onRate: (Int) -> Unit, onToggle: () -> Unit, onNext: () -> Unit,
    onPrevious: () -> Unit, onShuffle: () -> Unit, onRepeat: () -> Unit, onSeek: (Long) -> Unit) {
    Column(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF292137), Ink, Ink)))
        .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.KeyboardArrowDown, "Close now playing") }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = Lavender)
                Text(if (demo) "Sample preview" else "From your device", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            IconButton(onClick = onMore) { Icon(Icons.Rounded.MoreHoriz, "Song options") }
        }
        Spacer(Modifier.height(36.dp))
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) { RecordArt(Modifier.widthIn(max = 320.dp).fillMaxWidth().aspectRatio(1f)) }
        Spacer(Modifier.height(32.dp))
        Text(song.title, style = MaterialTheme.typography.headlineLarge)
        Text("${song.artist} · ${song.album}", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        RatingBar(song.rating, onRate)
        if (song.tags.isNotEmpty()) Text(song.tags.joinToString("  ·  "), color = Lavender, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))
        var dragging by remember(song.id) { mutableStateOf<Float?>(null) }
        val duration = if (demo) song.durationMs else state.durationMs
        Slider(value = dragging ?: state.positionMs.toFloat().coerceIn(0f, duration.coerceAtLeast(1).toFloat()), onValueChange = { dragging = it },
            onValueChangeFinished = { dragging?.let { onSeek(it.toLong()) }; dragging = null }, valueRange = 0f..duration.coerceAtLeast(1).toFloat(), enabled = !demo && duration > 0)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(durationText(if (demo) 0 else state.positionMs), color = Muted, style = MaterialTheme.typography.bodySmall); Text(durationText(duration), color = Muted, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onShuffle, enabled = !demo) { Icon(Icons.Rounded.Shuffle, if (state.shuffle) "Disable shuffle" else "Enable shuffle", tint = if (state.shuffle) Pink else Muted) }
            IconButton(onClick = onPrevious, enabled = !demo) { Icon(Icons.Rounded.SkipPrevious, "Previous song", Modifier.size(32.dp)) }
            FilledIconButton(onClick = onToggle, enabled = !demo, modifier = Modifier.size(72.dp), shape = CircleShape) { Icon(if (state.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (state.playing) "Pause" else "Play", Modifier.size(36.dp)) }
            IconButton(onClick = onNext, enabled = !demo) { Icon(Icons.Rounded.SkipNext, "Next song", Modifier.size(32.dp)) }
            IconButton(onClick = onRepeat, enabled = !demo) { Icon(if (state.repeat == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, "Repeat: ${when(state.repeat) { Player.REPEAT_MODE_ONE -> "one"; Player.REPEAT_MODE_ALL -> "all"; else -> "off" }}", tint = if (state.repeat != Player.REPEAT_MODE_OFF) Pink else Muted) }
        }
        Spacer(Modifier.height(24.dp))
        Text(if (demo) "Preview only · scan your music to listen" else "YOUR MUSIC. NO CONNECTION NEEDED.", color = Muted, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(24.dp))
    }
}
