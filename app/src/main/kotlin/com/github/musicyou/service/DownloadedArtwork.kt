package com.github.musicyou.service

import android.content.Context
import android.util.Log
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import com.github.musicyou.utils.thumbnail
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Artwork of downloaded songs, kept next to the downloads so covers still show offline.
 * Every size the app asks for is served from one high resolution copy.
 */
object DownloadedArtwork {
    private const val SIZE = 1200

    private lateinit var directory: File
    private val savedKeys: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun init(context: Context) {
        directory = context.filesDir.resolve("artwork").apply { mkdirs() }
        directory.listFiles()?.forEach { savedKeys.add(it.name) }
    }

    /** Safe to call from any thread; does nothing if already saved or the url is unknown. */
    fun save(url: String?) {
        val key = key(url) ?: return
        if (key in savedKeys) return

        val temporaryFile = directory.resolve("$key.tmp")

        runCatching {
            val connection = URL(url.thumbnail(SIZE)).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 16000
                connection.readTimeout = 8000
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    error("HTTP ${connection.responseCode}")
                }
                connection.inputStream.use { input ->
                    temporaryFile.outputStream().use { output -> input.copyTo(output) }
                }
            } finally {
                connection.disconnect()
            }

            if (!temporaryFile.renameTo(directory.resolve(key))) error("Couldn't move artwork")
            savedKeys.add(key)
        }.onFailure {
            temporaryFile.delete()
            Log.w("DownloadedArtwork", "Couldn't save artwork $url", it)
        }
    }

    fun delete(url: String?) {
        val key = key(url) ?: return
        savedKeys.remove(key)
        directory.resolve(key).delete()
    }

    fun isSameArtwork(url: String?, otherUrl: String?) = key(url) != null && key(url) == key(otherUrl)

    private fun file(url: String?) = key(url)?.takeIf { it in savedKeys }?.let(directory::resolve)

    // Removes the size parameters added by String.thumbnail so all sizes share one key
    private fun key(url: String?): String? {
        if (url == null || !url.startsWith("http")) return null

        val baseUrl = url
            .replace(Regex("-rj-.*$"), "-rj")
            .replace(Regex("-w\\d+-h\\d+(-s\\d+)?$"), "")

        return MessageDigest.getInstance("SHA-1")
            .digest(baseUrl.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /** Serves saved artwork from disk; any other image request is left untouched. */
    object ImageInterceptor : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
            val data = when (val data = chain.request.data) {
                is String, is android.net.Uri, is coil3.Uri -> data.toString()
                else -> null
            }

            val file = file(data) ?: return chain.proceed()

            return chain
                .withRequest(chain.request.newBuilder().data(file).build())
                .proceed()
        }
    }
}
