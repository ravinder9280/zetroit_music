package com.zetroit.music.viewmodels

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.innertube.Innertube
import com.github.innertube.requests.chartsPage
import com.github.innertube.requests.moodsAndGenres
import com.github.innertube.requests.newMusicVideos
import com.github.innertube.requests.newReleases
import com.github.innertube.requests.nextPage
import com.github.innertube.requests.playlistPage
import com.github.innertube.requests.relatedPage
import com.zetroit.music.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Songs and playlists picked for the user, and what they were based on. */
@Immutable
data class MadeForYou(
    val songs: List<Innertube.SongItem>,
    val playlists: List<Innertube.PlaylistItem>,
    val basedOnArtists: List<String>,
    // False when there is no listening history yet and popular music is suggested instead
    val isPersonalized: Boolean
)

/** The online sections of the home screen, each loaded and retried on its own. */
class HomeDiscoverViewModel : ViewModel() {
    var charts: Result<Innertube.ChartsPage>? by mutableStateOf(null)
        private set
    var trendingSongs: Result<List<Innertube.SongItem>>? by mutableStateOf(null)
        private set
    var newReleases: Result<List<Innertube.AlbumItem>>? by mutableStateOf(null)
        private set
    var newMusicVideos: Result<List<Innertube.SuggestedResult>>? by mutableStateOf(null)
        private set
    var moodsAndGenres: Result<Innertube.MoodsAndGenresPage>? by mutableStateOf(null)
        private set
    var madeForYou: Result<MadeForYou>? by mutableStateOf(null)
        private set

    private val jobs = mutableMapOf<String, Job>()

    /**
     * Starts loading a section unless it has loaded or is loading already. Sections ask for
     * their data when they first scroll into view, like YouTube Music's home only fetches more
     * shelves as you scroll. The work runs in the view model, so scrolling a section away again
     * does not cancel it halfway.
     */
    private fun loadOnce(key: String, isLoaded: Boolean, block: suspend () -> Unit): Job? {
        if (isLoaded) return null
        jobs[key]?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch { block() }.also { jobs[key] = it }
    }

    fun requestNewReleases() = loadOnce("newReleases", newReleases?.isSuccess == true) {
        newReleases = null
        newReleases = Innertube.newReleases(country = COUNTRY)
    }

    fun requestNewMusicVideos() = loadOnce("newMusicVideos", newMusicVideos?.isSuccess == true) {
        newMusicVideos = null
        newMusicVideos = Innertube.newMusicVideos(country = COUNTRY)
    }

    fun requestMoods() = loadOnce("moods", moodsAndGenres?.isSuccess == true) {
        moodsAndGenres = null
        moodsAndGenres = Innertube.moodsAndGenres(country = COUNTRY)
    }

    fun requestTrending() = loadOnce("trending", trendingSongs?.isSuccess == true) { loadTrending() }

    fun requestMadeForYou() = loadOnce("madeForYou", madeForYou?.isSuccess == true) { loadMadeForYou() }

    private suspend fun loadTrending() {
        charts = null
        trendingSongs = null

        val chartsResult = Innertube.chartsPage(country = COUNTRY) ?: return
        charts = chartsResult

        trendingSongs = chartsResult.mapCatching { page ->
            // The first chart is the one YouTube ranks by what is trending right now
            val trendingChart = page.charts.firstOrNull() ?: error("No charts for $COUNTRY")
            Innertube.playlistPage(browseId = trendingChart.key)
                ?.getOrThrow()
                ?.songsPage
                ?.items
                .orEmpty()
        }
    }

    private suspend fun loadMadeForYou() {
        madeForYou = null

        val (seeds, favoriteArtists, heardSongIds) = withContext(Dispatchers.IO) {
            Triple(
                (database.recentlyLikedSongs(limit = 2) + database.mostPlayedSongs(limit = 3))
                    .distinctBy { it.id }
                    .take(3)
                    .map { it.id },
                database.favoriteArtists(limit = 3).mapNotNull { it.name },
                database.playedSongIds().toSet()
            )
        }

        madeForYou = runCatching {
            if (seeds.isEmpty()) {
                requestTrending()?.join()
                val seed = trendingSongs?.getOrNull()?.firstOrNull()?.key
                    ?: error("Nothing to base recommendations on")

                MadeForYou(
                    songs = radio(listOf(seed)).filter { it.key != seed }.take(SONG_COUNT),
                    playlists = Innertube.relatedPage(videoId = seed)?.getOrNull()?.playlists.orEmpty(),
                    basedOnArtists = emptyList(),
                    isPersonalized = false
                )
            } else {
                val songs = radio(seeds).filter { it.key !in seeds }
                // Prefer songs the user has not listened to yet, unless that leaves too few
                val newSongs = songs.filter { it.key !in heardSongIds }

                MadeForYou(
                    songs = (if (newSongs.size >= SONG_COUNT / 2) newSongs else songs).take(SONG_COUNT),
                    playlists = Innertube.relatedPage(videoId = seeds.last())?.getOrNull()?.playlists.orEmpty(),
                    basedOnArtists = favoriteArtists,
                    isPersonalized = true
                )
            }
        }.mapCatching {
            if (it.songs.isEmpty() && it.playlists.isEmpty()) error("No recommendations")
            it
        }
    }

    /** The radios of each seed, interleaved so every seed is represented near the top. */
    private suspend fun radio(seeds: List<String>): List<Innertube.SongItem> = coroutineScope {
        val radios = seeds.map { seed ->
            async {
                Innertube.nextPage(videoId = seed)?.getOrNull()?.itemsPage?.items.orEmpty()
            }
        }.awaitAll()

        (0 until (radios.maxOfOrNull { it.size } ?: 0))
            .flatMap { index -> radios.mapNotNull { it.getOrNull(index) } }
            .distinctBy { it.key }
    }

    companion object {
        /** The home screen discovers music popular in India. */
        const val COUNTRY = "IN"

        // Fills whole columns of the four-row song grid
        private const val SONG_COUNT = 20
    }
}
