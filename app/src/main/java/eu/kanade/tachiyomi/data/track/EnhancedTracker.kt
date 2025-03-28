package eu.kanade.tachiyomi.data.track

import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.source.Source
import tachiyomi.domain.anime.model.Anime
import tachiyomi.domain.track.model.Track

/**
 * A tracker that will never prompt the user to manually bind an entry.
 * It is expected that such tracker can only work with specific sources and unique IDs.
 */
interface EnhancedTracker {

    /**
     * This tracker will only work with the sources that are accepted by this filter function.
     */
    fun accept(source: Source): Boolean {
        return source::class.qualifiedName in getAcceptedSources()
    }

    /**
     * Fully qualified source classes that this tracker is compatible with.
     */
    fun getAcceptedSources(): List<String>

    fun loginNoop()

    /**
     * Similar to [Tracker].search, but only returns zero or one match.
     */
    suspend fun match(anime: Anime): TrackSearch?

    /**
     * Checks whether the provided source/track/anime triplet is from this [Tracker]
     */
    fun isTrackFrom(track: Track, anime: Anime, source: Source?): Boolean

    /**
     * Migrates the given track for the anime to the newSource, if possible
     */
    fun migrateTrack(track: Track, anime: Anime, newSource: Source): Track?
}
