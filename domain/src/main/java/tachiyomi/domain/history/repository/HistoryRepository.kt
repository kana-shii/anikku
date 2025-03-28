package tachiyomi.domain.history.repository

import kotlinx.coroutines.flow.Flow
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.HistoryUpdate
import tachiyomi.domain.history.model.HistoryWithRelations

interface HistoryRepository {

    fun getHistory(query: String): Flow<List<HistoryWithRelations>>

    suspend fun getLastHistory(): HistoryWithRelations?

    suspend fun getTotalWatchDuration(): Long

    suspend fun getHistoryByAnimeId(animeId: Long): List<History>

    suspend fun resetHistory(historyId: Long)

    suspend fun resetHistoryByAnimeId(animeId: Long)

    suspend fun deleteAllHistory(): Boolean

    suspend fun upsertHistory(historyUpdate: HistoryUpdate)

    // SY -->
    suspend fun upsertHistory(historyUpdates: List<HistoryUpdate>)

    suspend fun getByAnimeId(animeId: Long): List<History>
    // SY <--
}
