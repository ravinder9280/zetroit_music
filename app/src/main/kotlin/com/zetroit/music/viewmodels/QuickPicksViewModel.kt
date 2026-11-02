package com.zetroit.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.innertube.Innertube
import com.github.innertube.requests.nextPage
import com.github.innertube.requests.relatedPage
import com.zetroit.music.database
import com.zetroit.music.enums.QuickPicksSource
import com.zetroit.music.models.Song
import kotlinx.coroutines.flow.distinctUntilChanged

class QuickPicksViewModel : ViewModel() {
    var trending: Song? by mutableStateOf(null)
    var relatedPageResult: Result<Innertube.RelatedPage?>? by mutableStateOf(null)

    suspend fun loadQuickPicks(quickPicksSource: QuickPicksSource) {
        val flow = when (quickPicksSource) {
            QuickPicksSource.Trending -> database.trending()
            QuickPicksSource.LastPlayed -> database.lastPlayed()
            QuickPicksSource.Random -> database.randomSong()
        }

        flow.distinctUntilChanged().collect { song ->
            if (quickPicksSource == QuickPicksSource.Random && song != null && trending != null) return@collect

            if ((song == null && relatedPageResult == null) || trending?.id != song?.id || relatedPageResult?.isSuccess != true) {
                relatedPageResult = loadRelatedPage(videoId = song?.id ?: "fJ9rUzIMcZQ")
            }

            trending = song
        }
    }

    private suspend fun loadRelatedPage(videoId: String) =
        Innertube.relatedPage(videoId = videoId)?.map { relatedPage ->
            // YouTube caps "You might also like", so continue with the song's radio
            val radioSongs = Innertube.nextPage(videoId = videoId)
                ?.getOrNull()?.itemsPage?.items.orEmpty()

            relatedPage?.copy(
                songs = (relatedPage.songs.orEmpty() + radioSongs)
                    .distinctBy(Innertube.SongItem::key)
                    .filter { it.key != videoId }
                    .take(QUICK_PICKS_LIMIT)
            )
        }

    private companion object {
        const val QUICK_PICKS_LIMIT = 48
    }
}