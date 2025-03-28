package eu.kanade.tachiyomi.ui.browse.migration.advanced.process

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.domain.anime.model.Anime
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import java.text.DecimalFormat
import kotlin.coroutines.CoroutineContext

class MigratingAnime(
    val manga: Anime,
    val episodeInfo: EpisodeInfo,
    val sourcesString: String,
    parentContext: CoroutineContext,
) {
    val migrationScope = CoroutineScope(parentContext + SupervisorJob() + Dispatchers.Default)

    // KMK -->
    var searchingJob: Deferred<Anime?>? = null
    // KMK <--

    val searchResult = MutableStateFlow<SearchResult>(SearchResult.Searching)

    // <MAX, PROGRESS>
    val progress = MutableStateFlow(1 to 0)

    sealed class SearchResult {
        data object Searching : SearchResult()
        data object NotFound : SearchResult()
        data class Result(val id: Long) : SearchResult()
    }

    data class EpisodeInfo(
        val latestChapter: Double?,
        val chapterCount: Int,
    ) {
        fun getFormattedLatestChapter(context: Context): String {
            return if (latestChapter != null && latestChapter > 0.0) {
                context.stringResource(
                    SYMR.strings.latest_,
                    DecimalFormat("#.#").format(latestChapter),
                )
            } else {
                context.stringResource(
                    SYMR.strings.latest_,
                    context.stringResource(MR.strings.unknown),
                )
            }
        }
    }
}
