package app.byos.android.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF10101B)
val Panel = Color(0xFF1C1B2D)
val Lavender = Color(0xFFCEACFF)
val Pink = Color(0xFFF3A5DB)
val Blue = Color(0xFF8EBDFF)
val Muted = Color(0xFFB5B1C8)
val Glow = listOf(Pink, Lavender, Blue)
private val Colors = darkColorScheme(primary = Lavender, onPrimary = Color(0xFF271044),
    secondary = Pink, onSecondary = Color(0xFF41102F), tertiary = Blue,
    background = Ink, onBackground = Color(0xFFF5F1FF), surface = Ink, onSurface = Color(0xFFF5F1FF),
    surfaceVariant = Panel, onSurfaceVariant = Muted, outline = Color(0xFF77718C))
private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp),
)
@Composable fun ByosTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = Colors, typography = Type, content = content) }
