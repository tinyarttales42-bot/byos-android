package app.byos.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.byos.android.model.Song
import app.byos.android.ui.theme.*
import java.util.Locale

fun durationText(ms: Long): String = String.format(Locale.ROOT, "%d:%02d", ms / 60000, ms / 1000 % 60)

@Composable fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).background(Brush.linearGradient(listOf(Color(0xFF30283F), Panel)))
        .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(24.dp)).padding(20.dp), content = content)
}

@Composable fun RecordArt(modifier: Modifier = Modifier, variant: Int = 0) {
    val colors = if (variant % 2 == 0) Glow else listOf(Blue, Lavender, Pink)
    Canvas(modifier.clip(CircleShape).background(Brush.linearGradient(colors)).semantics { contentDescription = "Gradient vinyl artwork" }) {
        val radius = size.minDimension / 2
        for (i in 2..8) drawCircle(Color.White.copy(alpha = .20f), radius * i / 9, style = Stroke(radius * .007f))
        drawCircle(Color.White.copy(alpha = .7f), radius * .23f)
        drawCircle(Ink, radius * .055f)
        drawLine(Color.White.copy(alpha = .17f), Offset(radius * .32f, radius * .4f), Offset(radius * 1.65f, radius * 1.5f), radius * .12f)
    }
}

@Composable fun SongRow(song: Song, index: Int, onClick: () -> Unit, onMore: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(if (index % 2 == 0) listOf(Color(0xFF533F75), Color(0xFF253C61)) else listOf(Color(0xFF563951), Color(0xFF3A3165)))), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.MusicNote, null, tint = Lavender, modifier = Modifier.size(26.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(durationText(song.durationMs), color = Muted, style = MaterialTheme.typography.labelMedium)
        IconButton(onClick = onMore) { Icon(Icons.Rounded.MoreHoriz, "Options for ${song.title}", tint = Muted) }
    }
}

@Composable fun RatingBar(rating: Int, onRate: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        (1..5).forEach { stars -> IconButton(onClick = { onRate(stars) }) {
            Icon(if (stars <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                "Rate $stars ${if (stars == 1) "star" else "stars"}", tint = if (stars <= rating) Pink else Muted)
        } }
    }
}
