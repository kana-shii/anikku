package eu.kanade.domain.anime.model

import eu.kanade.domain.base.BasePreferences
import eu.kanade.tachiyomi.data.cache.CoverCache
import eu.kanade.tachiyomi.source.model.SAnime
import tachiyomi.core.common.preference.TriState
import tachiyomi.domain.anime.model.Anime
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

// TODO: move these into the domain model
val Anime.downloadedFilter: TriState
    get() {
        if (forceDownloaded()) return TriState.ENABLED_IS
        return when (downloadedFilterRaw) {
            Anime.EPISODE_SHOW_DOWNLOADED -> TriState.ENABLED_IS
            Anime.EPISODE_SHOW_NOT_DOWNLOADED -> TriState.ENABLED_NOT
            else -> TriState.DISABLED
        }
    }
fun Anime.episodesFiltered(): Boolean {
    return unseenFilter != TriState.DISABLED ||
        downloadedFilter != TriState.DISABLED ||
        bookmarkedFilter != TriState.DISABLED ||
        // AM (FILLERMARK) -->
        fillermarkedFilter != TriState.DISABLED
    // <-- AM (FILLERMARK)
}
fun Anime.forceDownloaded(): Boolean {
    return favorite && Injekt.get<BasePreferences>().downloadedOnly().get()
}

fun Anime.toSAnime(): SAnime = SAnime.create().also {
    it.url = url
    it.title = title
    it.artist = artist
    it.author = author
    it.description = description
    it.genre = genre.orEmpty().joinToString()
    it.status = status.toInt()
    it.thumbnail_url = thumbnailUrl
    it.initialized = initialized
}

fun Anime.copyFrom(other: SAnime): Anime {
    // SY -->
    val author = other.author ?: ogAuthor
    val artist = other.artist ?: ogArtist
    val thumbnailUrl = other.thumbnail_url ?: ogThumbnailUrl
    val description = other.description ?: ogDescription
    val genres = if (other.genre != null) {
        other.getGenres()
    } else {
        ogGenre
    }
    // SY <--
    return this.copy(
        // SY -->
        ogAuthor = author,
        ogArtist = artist,
        ogThumbnailUrl = thumbnailUrl,
        ogDescription = description,
        ogGenre = genres,
        // SY <--
        // SY -->
        ogStatus = other.status.toLong(),
        // SY <--
        updateStrategy = other.update_strategy,
        initialized = other.initialized && initialized,
    )
}

fun SAnime.toDomainAnime(sourceId: Long): Anime {
    return Anime.create().copy(
        url = url,
        // SY -->
        ogTitle = title,
        ogArtist = artist,
        ogAuthor = author,
        ogThumbnailUrl = thumbnail_url,
        ogDescription = description,
        ogGenre = getGenres(),
        ogStatus = status.toLong(),
        // SY <--
        updateStrategy = update_strategy,
        initialized = initialized,
        source = sourceId,
    )
}

fun Anime.hasCustomCover(coverCache: CoverCache = Injekt.get()): Boolean {
    return coverCache.getCustomCoverFile(id).exists()
}
