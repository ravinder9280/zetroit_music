package com.zetroit.music.service

import android.app.Notification
import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.ContentMetadataMutations
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.github.innertube.Innertube
import com.github.innertube.requests.player
import com.zetroit.music.R
import com.zetroit.music.database
import com.zetroit.music.models.Format
import com.zetroit.music.models.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.min

/**
 * Songs the user explicitly downloads. They are stored in their own cache inside [Context.getFilesDir]
 * without an evictor, so unlike the playback cache they are never removed automatically.
 */
@OptIn(UnstableApi::class)
object SongDownloads {
    private const val CHUNK_LENGTH = 512 * 1024L

    private lateinit var context: Context
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val streamUrls = ConcurrentHashMap<String, Uri>()

    val databaseProvider by lazy { StandaloneDatabaseProvider(context) }

    val cache by lazy {
        SimpleCache(context.filesDir.resolve("downloads"), NoOpCacheEvictor(), databaseProvider)
    }

    lateinit var downloadManager: DownloadManager
        private set

    private val mutableDownloads = MutableStateFlow<Map<String, Download>>(emptyMap())
    val downloads: StateFlow<Map<String, Download>> = mutableDownloads.asStateFlow()

    fun init(context: Context) {
        this.context = context.applicationContext
        DownloadedArtwork.init(this.context)

        downloadManager = DownloadManager(
            this.context,
            databaseProvider,
            cache,
            createDataSourceFactory(),
            Executors.newFixedThreadPool(2)
        ).apply {
            maxParallelDownloads = 2
            addListener(object : DownloadManager.Listener {
                override fun onInitialized(downloadManager: DownloadManager) {
                    coroutineScope.launch {
                        val all = buildMap {
                            downloadManager.downloadIndex.getDownloads().use { cursor ->
                                while (cursor.moveToNext()) put(cursor.download.request.id, cursor.download)
                            }
                        }
                        mutableDownloads.update { all + it }

                        // Saves artwork of songs downloaded before it was kept
                        all.values
                            .filter { it.state == Download.STATE_COMPLETED }
                            .forEach { DownloadedArtwork.save(thumbnailUrl(it.request.id)) }
                    }
                }

                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    mutableDownloads.update { it + (download.request.id to download) }

                    if (download.state == Download.STATE_COMPLETED) coroutineScope.launch {
                        DownloadedArtwork.save(thumbnailUrl(download.request.id))
                    }

                    if (download.state == Download.STATE_FAILED) {
                        Toast.makeText(
                            this@SongDownloads.context,
                            this@SongDownloads.context.getString(
                                R.string.download_failed,
                                download.request.data.decodeToString()
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                    mutableDownloads.update { it - download.request.id }
                    streamUrls.remove(download.request.id)

                    // Songs of the same album share artwork, so keep it while one is downloaded
                    coroutineScope.launch {
                        val url = thumbnailUrl(download.request.id)
                        val isShared = downloads.value.values
                            .filter { it.state == Download.STATE_COMPLETED }
                            .any { DownloadedArtwork.isSameArtwork(url, thumbnailUrl(it.request.id)) }

                        if (!isShared) DownloadedArtwork.delete(url)
                    }
                }
            })
        }
    }

    private suspend fun thumbnailUrl(songId: String) = database.song(songId).first()?.thumbnailUrl

    fun isDownloaded(songId: String) = downloads.value[songId]?.state == Download.STATE_COMPLETED

    /** Completed downloads, most recently downloaded first. */
    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    fun downloadedSongs(): Flow<List<Song>> = downloads
        .map { downloads ->
            downloads.values
                .filter { it.state == Download.STATE_COMPLETED }
                .sortedByDescending(Download::startTimeMs)
                .map { it.request.id }
        }
        .distinctUntilChanged()
        .flatMapLatest { ids ->
            database.songs(ids).map { songs -> songs.sortedBy { ids.indexOf(it.id) } }
        }

    fun download(context: Context, mediaItem: MediaItem) {
        val songId = mediaItem.mediaId
        val title = mediaItem.mediaMetadata.title?.toString() ?: songId

        coroutineScope.launch {
            val contentLength = runCatching {
                database.insert(mediaItem)
                resolve(songId).second
            }.onFailure { Log.w("SongDownloads", "Couldn't start download of $songId", it) }.getOrNull()

            withContext(Dispatchers.Main) {
                if (contentLength == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.download_failed, title),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@withContext
                }

                // Lets cached reads know where the song ends without asking the network
                cache.applyContentMetadataMutations(
                    songId,
                    ContentMetadataMutations().also {
                        ContentMetadataMutations.setContentLength(it, contentLength)
                    }
                )

                val request = DownloadRequest.Builder(songId, songId.toUri())
                    .setCustomCacheKey(songId)
                    .setByteRange(0, contentLength)
                    .setData(title.encodeToByteArray())
                    .build()

                DownloadService.sendAddDownload(context, SongDownloadService::class.java, request, false)
            }
        }
    }

    fun remove(context: Context, songId: String) {
        DownloadService.sendRemoveDownload(context, SongDownloadService::class.java, songId, false)
    }

    private suspend fun resolve(songId: String): Pair<Uri, Long> {
        val body = Innertube.player(videoId = songId)?.getOrThrow()
            ?: throw IOException("No response for $songId")

        if (body.videoDetails?.videoId != songId) throw VideoIdMismatchException()
        if (body.playabilityStatus?.status != "OK") throw UnplayableException()

        val format = body.streamingData?.highestQualityFormat
            ?: throw PlayableFormatNotFoundException()
        val url = format.url?.toUri() ?: throw PlayableFormatNotFoundException()
        val contentLength = format.contentLength ?: throw PlayableFormatNotFoundException()

        format.approxDurationMs?.div(1000)
            ?.let(DateUtils::formatElapsedTime)?.removePrefix("0")
            ?.let { durationText -> database.updateDurationText(songId, durationText) }

        database.insert(
            Format(
                songId = songId,
                itag = format.itag,
                mimeType = format.mimeType,
                bitrate = format.bitrate,
                loudnessDb = body.playerConfig?.audioConfig?.normalizedLoudnessDb,
                contentLength = contentLength,
                lastModified = format.lastModified
            )
        )

        streamUrls[songId] = url
        return url to contentLength
    }

    private fun createDataSourceFactory(): DataSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(16000)
            .setReadTimeoutMs(8000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0")

        return ResolvingDataSource.Factory(
            ChunkedDataSource.Factory(httpDataSourceFactory, CHUNK_LENGTH)
        ) { dataSpec ->
            val songId = dataSpec.key ?: dataSpec.uri.toString()
            val url = streamUrls[songId] ?: runBlocking { resolve(songId).first }
            dataSpec.withUri(url)
        }
    }

    /**
     * Requests the stream in [chunkLength] ranges, as playback does, while presenting it to the
     * cache as a single continuous read so the download is not mistaken for a truncated file.
     */
    private class ChunkedDataSource(
        private val upstream: DataSource,
        private val chunkLength: Long
    ) : DataSource {
        private var dataSpec: DataSpec? = null
        private var offset = 0L
        private var bytesRemaining = 0L
        private var chunkBytesRead = 0L

        override fun addTransferListener(transferListener: TransferListener) =
            upstream.addTransferListener(transferListener)

        override fun open(dataSpec: DataSpec): Long {
            this.dataSpec = dataSpec
            offset = 0
            bytesRemaining = dataSpec.length

            if (dataSpec.length == C.LENGTH_UNSET.toLong()) return upstream.open(dataSpec)

            openChunk()
            return dataSpec.length
        }

        private fun openChunk() {
            chunkBytesRead = 0
            upstream.open(dataSpec!!.subrange(offset, min(chunkLength, bytesRemaining)))
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (bytesRemaining == C.LENGTH_UNSET.toLong()) return upstream.read(buffer, offset, length)
            if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

            val read = upstream.read(buffer, offset, min(length.toLong(), bytesRemaining).toInt())

            if (read == C.RESULT_END_OF_INPUT) {
                if (chunkBytesRead == 0L) throw IOException("Stream ended $bytesRemaining bytes early")
                upstream.close()
                openChunk()
                return read(buffer, offset, length)
            }

            this.offset += read
            bytesRemaining -= read
            chunkBytesRead += read
            return read
        }

        override fun getUri() = upstream.uri

        override fun getResponseHeaders() = upstream.responseHeaders

        override fun close() {
            dataSpec = null
            upstream.close()
        }

        class Factory(
            private val upstreamFactory: DataSource.Factory,
            private val chunkLength: Long
        ) : DataSource.Factory {
            override fun createDataSource() =
                ChunkedDataSource(upstreamFactory.createDataSource(), chunkLength)
        }
    }
}

@OptIn(UnstableApi::class)
class SongDownloadService : DownloadService(
    NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    NOTIFICATION_CHANNEL_ID,
    R.string.downloads,
    0
) {
    private val notificationHelper by lazy { DownloadNotificationHelper(this, NOTIFICATION_CHANNEL_ID) }

    override fun getDownloadManager() = SongDownloads.downloadManager

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification = notificationHelper.buildProgressNotification(
        this,
        R.drawable.app_icon,
        null,
        downloads.singleOrNull()?.request?.data?.decodeToString(),
        downloads,
        notMetRequirements
    )

    private companion object {
        const val NOTIFICATION_ID = 1003
        const val NOTIFICATION_CHANNEL_ID = "download_channel_id"
    }
}
