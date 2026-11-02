package com.zetroit.music

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import com.github.innertube.Innertube
import com.github.innertube.requests.visitorData
import com.zetroit.music.database.DatabaseInitializer
import com.zetroit.music.enums.CoilDiskCacheMaxSize
import com.zetroit.music.service.DownloadedArtwork
import com.zetroit.music.service.SongDownloads
import com.zetroit.music.utils.coilDiskCacheMaxSizeKey
import com.zetroit.music.utils.getEnum
import com.zetroit.music.utils.preferences
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainApplication : Application(), SingletonImageLoader.Factory {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        database = DatabaseInitializer.newInstance(context = applicationContext)
        SongDownloads.init(context = applicationContext)

        GlobalScope.launch {
            if (Innertube.visitorData.isNullOrBlank()) Innertube.visitorData =
                Innertube.visitorData().getOrNull()
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components { add(DownloadedArtwork.ImageInterceptor) }
            .diskCache(
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }
}