package app.byos.android.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class LibraryRulesTest {
    @Test fun labelsCollapseWhitespaceAndIgnoreCase() {
        assertEquals("late night", labelKey("  LATE  \t night "))
        assertEquals("Late night", normalizeLabel("  Late   night  "))
    }
    @Test fun keysDoNotChangeWithDeviceLocale() {
        val original = Locale.getDefault()
        try { Locale.setDefault(Locale.forLanguageTag("tr-TR")); assertEquals("indie", labelKey("INDIE")) }
        finally { Locale.setDefault(original) }
    }
    @Test fun ratingsAcceptOneThroughFive() { (1..5).forEach { assertEquals(it, validRating(it)) } }
    @Test(expected = IllegalArgumentException::class) fun rejectsZeroStars() { validRating(0) }
    @Test(expected = IllegalArgumentException::class) fun rejectsSixStars() { validRating(6) }
    @Test fun searchMatchesArtistAlbumAndTagsWithoutCase() {
        val songs = DemoLibrary.songs
        assertEquals(2, songs.filtered("LUNA", SongSort.TITLE, false).size)
        assertEquals(2, songs.filtered("violet", SongSort.TITLE, false).size)
        assertEquals(2, songs.filtered("LATE NIGHT", SongSort.TITLE, false).size)
    }
    @Test fun filtersComposeAndRatingsSortDescending() {
        val songs = DemoLibrary.songs.filtered("", SongSort.RATING, true, "chill")
        assertEquals(listOf("Afterglow", "Golden"), songs.map { it.title })
        assertTrue(songs.all { it.rating >= 4 })
    }
    @Test fun unknownQueryReturnsEmptyList() { assertTrue(DemoLibrary.songs.filtered("missing", SongSort.TITLE, false).isEmpty()) }
    @Test fun samplesCannotBePlayedAsDeviceFiles() { assertTrue(DemoLibrary.songs.all { it.uri == null }) }
}
