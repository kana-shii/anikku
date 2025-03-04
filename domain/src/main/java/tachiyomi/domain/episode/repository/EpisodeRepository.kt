package tachiyomi.domain.episode.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.episode.model.Episode
import tachiyomi.domain.episode.model.EpisodeUpdate

interface EpisodeRepository {

    suspend fun addAll(episodes: List<Episode>): List<Episode>

    suspend fun update(episodeUpdate: EpisodeUpdate)

    suspend fun updateAll(episodeUpdates: List<EpisodeUpdate>)

    suspend fun removeEpisodesWithIds(episodeIds: List<Long>)

    suspend fun getEpisodeByAnimeId(animeId: Long): List<Episode>

    suspend fun getBookmarkedEpisodesByAnimeId(animeId: Long): List<Episode>

    // AM (FILLERMARK) -->
    suspend fun getFillermarkedEpisodesByAnimeId(animeId: Long): List<Episode>
    // <-- AM (FILLERMARK)

    suspend fun getEpisodeById(id: Long): Episode?

    suspend fun getEpisodeByAnimeIdAsFlow(animeId: Long): Flow<List<Episode>>

    suspend fun getEpisodeByUrlAndAnimeId(url: String, animeId: Long): Episode?

    // SY -->
    suspend fun getEpisodeByUrl(url: String): List<Episode>

    suspend fun getMergedEpisodeByAnimeId(animeId: Long): List<Episode>

    suspend fun getMergedEpisodeByAnimeIdAsFlow(
        animeId: Long,
    ): Flow<List<Episode>>
    // SY <--
}
