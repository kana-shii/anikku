package tachiyomi.presentation.widget.components

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import tachiyomi.presentation.widget.R
import tachiyomi.presentation.widget.util.appWidgetInnerRadius

val CoverWidth = 58.dp
val CoverHeight = 87.dp

@Composable
fun UpdatesAnimeCover(
    cover: Bitmap?,
    modifier: GlanceModifier = GlanceModifier,
    // KMK -->
    color: Color? = null,
    // KMK <--
) {
    Box(
        modifier = modifier
            .size(width = CoverWidth, height = CoverHeight)
            // KMK -->
            .background(color ?: Color.Unspecified)
            // KMK <--
            .appWidgetInnerRadius(),
    ) {
        if (cover != null) {
            Image(
                provider = ImageProvider(cover),
                contentDescription = null,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetInnerRadius(),
                contentScale = ContentScale.Crop,
            )
        } else {
            // Enjoy placeholder
            Image(
                provider = ImageProvider(R.drawable.appwidget_cover_error),
                contentDescription = null,
                modifier = GlanceModifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
