package mihon.feature.upcoming.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.anime.components.AnimeCover
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.anime.model.asAnimeCover
import tachiyomi.presentation.core.components.material.padding

private val UpcomingItemHeight = 96.dp

@Composable
fun UpcomingItem(
    upcoming: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(UpcomingItemHeight)
            .padding(
                horizontal = MaterialTheme.padding.medium,
                vertical = MaterialTheme.padding.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.large),
    ) {
        // KMK -->
        val mangaCover = upcoming.asAnimeCover()
        val bgColor = mangaCover.dominantCoverColors?.first?.let { Color(it) }
        val onBgColor = mangaCover.dominantCoverColors?.second
        // KMK <--
        AnimeCover.Book(
            modifier = Modifier.fillMaxHeight(),
            data = mangaCover,
            bgColor = bgColor,
            tint = onBgColor,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = upcoming.title,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
