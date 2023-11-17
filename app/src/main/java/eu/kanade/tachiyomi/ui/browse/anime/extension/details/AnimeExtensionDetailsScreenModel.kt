package eu.kanade.tachiyomi.ui.browse.anime.extension.details

import android.content.Context
import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import eu.kanade.domain.extension.anime.interactor.AnimeExtensionSourceItem
import eu.kanade.domain.extension.anime.interactor.GetAnimeExtensionSources
import eu.kanade.domain.source.anime.interactor.ToggleAnimeSource
import eu.kanade.tachiyomi.extension.anime.AnimeExtensionManager
import eu.kanade.tachiyomi.extension.anime.model.AnimeExtension
import eu.kanade.tachiyomi.network.NetworkHelper
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.system.LocaleHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.LogPriority
import okhttp3.HttpUrl.Companion.toHttpUrl
import tachiyomi.core.util.system.logcat
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

private const val URL_EXTENSION_COMMITS =
    "https://github.com/aniyomiorg/aniyomi-extensions/commits/master"
private const val URL_EXTENSION_BLOB =
    "https://github.com/aniyomiorg/aniyomi-extensions/blob/master"

class AnimeExtensionDetailsScreenModel(
    pkgName: String,
    context: Context,
    private val network: NetworkHelper = Injekt.get(),
    private val extensionManager: AnimeExtensionManager = Injekt.get(),
    private val getExtensionSources: GetAnimeExtensionSources = Injekt.get(),
    private val toggleSource: ToggleAnimeSource = Injekt.get(),
) : StateScreenModel<AnimeExtensionDetailsScreenModel.State>(State()) {

    private val _events: Channel<AnimeExtensionDetailsEvent> = Channel()
    val events: Flow<AnimeExtensionDetailsEvent> = _events.receiveAsFlow()

    init {
        coroutineScope.launch {
            launch {
                extensionManager.installedExtensionsFlow
                    .map { it.firstOrNull { extension -> extension.pkgName == pkgName } }
                    .collectLatest { extension ->
                        if (extension == null) {
                            _events.send(AnimeExtensionDetailsEvent.Uninstalled)
                            return@collectLatest
                        }
                        mutableState.update { state ->
                            state.copy(extension = extension)
                        }
                    }
            }
            launch {
                state.collectLatest { state ->
                    if (state.extension == null) return@collectLatest
                    getExtensionSources.subscribe(state.extension)
                        .map {
                            it.sortedWith(
                                compareBy(
                                    { !it.enabled },
                                    { item ->
                                        item.source.name.takeIf { item.labelAsName }
                                            ?: LocaleHelper.getSourceDisplayName(item.source.lang, context).lowercase()
                                    },
                                ),
                            )
                        }
                        .catch { throwable ->
                            logcat(LogPriority.ERROR, throwable)
                            mutableState.update { it.copy(_sources = emptyList()) }
                        }
                        .collectLatest { sources ->
                            mutableState.update { it.copy(_sources = sources) }
                        }
                }
            }
        }
    }

    fun getChangelogUrl(): String {
        val extension = state.value.extension ?: return ""

        val pkgName = extension.pkgName.substringAfter("eu.kanade.tachiyomi.animeextension.")
        val pkgFactory = extension.pkgFactory
        if (extension.hasChangelog) {
            return createUrl(URL_EXTENSION_BLOB, pkgName, pkgFactory, "/CHANGELOG.md")
        }

        // Falling back on GitHub commit history because there is no explicit changelog in extension
        return createUrl(URL_EXTENSION_COMMITS, pkgName, pkgFactory)
    }

    fun getReadmeUrl(): String {
        val extension = state.value.extension ?: return ""

        if (!extension.hasReadme) {
            return "https://aniyomi.org/help/faq/#extensions"
        }

        val pkgName = extension.pkgName.substringAfter("eu.kanade.tachiyomi.animeextension.")
        val pkgFactory = extension.pkgFactory
        return createUrl(URL_EXTENSION_BLOB, pkgName, pkgFactory, "/README.md")
    }

    fun clearCookies() {
        val extension = state.value.extension ?: return

        val urls = extension.sources
            .filterIsInstance<HttpSource>()
            .mapNotNull { it.baseUrl.takeUnless { url -> url.isEmpty() } }
            .distinct()

        val cleared = urls.sumOf {
            try {
                network.cookieJar.remove(it.toHttpUrl())
            } catch (e: Exception) {
                logcat(LogPriority.ERROR, e) { "Failed to clear cookies for $it" }
                0
            }
        }

        logcat { "Cleared $cleared cookies for: ${urls.joinToString()}" }
    }

    fun uninstallExtension() {
        val extension = state.value.extension ?: return
        extensionManager.uninstallExtension(extension.pkgName)
    }

    fun toggleSource(sourceId: Long) {
        toggleSource.await(sourceId)
    }

    fun toggleSources(enable: Boolean) {
        state.value.extension?.sources
            ?.map { it.id }
            ?.let { toggleSource.await(it, enable) }
    }

    private fun createUrl(
        url: String,
        pkgName: String,
        pkgFactory: String?,
        path: String = "",
    ): String {
        return if (!pkgFactory.isNullOrEmpty()) {
            when (path.isEmpty()) {
                true -> "$url/multisrc/src/main/java/eu/kanade/tachiyomi/multisrc/$pkgFactory"
                else -> "$url/multisrc/overrides/$pkgFactory/" + (pkgName.split(".").lastOrNull() ?: "") + path
            }
        } else {
            url + "/src/" + pkgName.replace(".", "/") + path
        }
    }

    @Immutable
    data class State(
        val extension: AnimeExtension.Installed? = null,
        private val _sources: List<AnimeExtensionSourceItem>? = null,
    ) {

        val sources: List<AnimeExtensionSourceItem>
            get() = _sources.orEmpty()

        val isLoading: Boolean
            get() = extension == null || _sources == null
    }
}

sealed interface AnimeExtensionDetailsEvent {
    data object Uninstalled : AnimeExtensionDetailsEvent
}
