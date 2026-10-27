package app.byos.android.model

import java.util.Locale

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: String? = null,
    val rating: Int = 0,
    val tags: Set<String> = emptySet(),
)

enum class SongSort { TITLE, ARTIST, RATING }

fun normalizeLabel(value: String): String = value.trim().replace(Regex("\\s+"), " ")
fun labelKey(value: String): String = normalizeLabel(value).lowercase(Locale.ROOT)
fun validRating(value: Int): Int {
    require(value in 1..5) { "Rating must be between 1 and 5" }
    return value
}

fun List<Song>.filtered(query: String, sort: SongSort, favoritesOnly: Boolean, tag: String? = null): List<Song> {
    val needle = query.trim()
    val matching = filter { song ->
        (!favoritesOnly || song.rating >= 4) &&
            (tag == null || song.tags.any { labelKey(it) == labelKey(tag) }) &&
            (needle.isEmpty() || listOf(song.title, song.artist, song.album).plus(song.tags)
                .any { it.contains(needle, ignoreCase = true) })
    }
    return when (sort) {
        SongSort.TITLE -> matching.sortedBy { it.title.lowercase(Locale.ROOT) }
        SongSort.ARTIST -> matching.sortedWith(compareBy<Song> { it.artist.lowercase(Locale.ROOT) }.thenBy { it.title })
        SongSort.RATING -> matching.sortedWith(compareByDescending<Song> { it.rating }.thenBy { it.title })
    }
}

object DemoLibrary {
    val songs = listOf(
        Song("demo-1", "Afterglow", "Luna Bay", "Violet Hours", 237000, rating = 5, tags = setOf("Late night", "Chill")),
        Song("demo-2", "Blue Horizon", "The Paper Planes", "Somewhere Else", 264000, rating = 4, tags = setOf("Focus")),
        Song("demo-3", "Golden", "Mira Sol", "Soft Focus", 200000, rating = 5, tags = setOf("Chill")),
        Song("demo-4", "Midnight Drive", "Northbound", "City Lights", 218000, rating = 3, tags = setOf("Late night")),
        Song("demo-5", "Slow Motion", "Luna Bay", "Violet Hours", 192000, rating = 4, tags = setOf("Focus")),
        Song("demo-6", "Weightless", "Ocean State", "Open Water", 281000, rating = 0),
    )
}
