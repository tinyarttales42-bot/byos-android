package app.byos.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun sampleSearchAndPlayerAreAccessible() {
        compose.onNodeWithText("Explore sample library").performScrollTo().performClick()
        compose.onNodeWithText("Find a song, artist or tag").performScrollTo().performTextInput("Afterglow")
        compose.onNode(hasText("Afterglow") and !hasSetTextAction()).performScrollTo().performClick()
        compose.onNodeWithText("NOW PLAYING").assertIsDisplayed()
        compose.onNodeWithContentDescription("Rate 5 stars").assertIsDisplayed()
        compose.onNodeWithContentDescription("Play").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Close now playing").performClick()
        compose.onNodeWithText("The collection").assertExists()
    }
    @Test fun navigateToPlaylistAndTagEmptyStates() {
        compose.onNodeWithText("Playlists").performClick()
        compose.onNodeWithText("Create playlist", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Tags").performClick()
        compose.onNodeWithText("Create tag", substring = true).assertIsDisplayed()
    }
}
