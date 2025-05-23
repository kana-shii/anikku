package tachiyomi.source.local.image

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.source.model.SAnime
import eu.kanade.tachiyomi.util.storage.DiskUtil
import tachiyomi.core.common.storage.nameWithoutExtension
import tachiyomi.core.common.util.system.ImageUtil
import tachiyomi.source.local.io.LocalSourceFileSystem
import java.io.InputStream

private const val DEFAULT_COVER_NAME = "cover.jpg"

actual class LocalCoverManager(
    private val context: Context,
    private val fileSystem: LocalSourceFileSystem,
) {

    actual fun find(animeUrl: String): UniFile? {
        return fileSystem.getFilesInAnimeDirectory(animeUrl)
            // Get all file whose names start with "cover"
            .filter { it.isFile && it.nameWithoutExtension.equals("cover", ignoreCase = true) }
            // Get the first actual image
            .firstOrNull { ImageUtil.isImage(it.name) { it.openInputStream() } }
    }

    actual fun update(anime: SAnime, inputStream: InputStream): UniFile? {
        val directory = fileSystem.getAnimeDirectory(anime.url)
        if (directory == null) {
            inputStream.close()
            return null
        }

        val targetFile = find(anime.url) ?: directory.createFile(DEFAULT_COVER_NAME)!!

        inputStream.use { input ->
            targetFile.openOutputStream().use { output ->
                input.copyTo(output)
            }
        }

        DiskUtil.createNoMediaFile(directory, context)

        anime.thumbnail_url = targetFile.uri.toString()
        return targetFile
    }
}
