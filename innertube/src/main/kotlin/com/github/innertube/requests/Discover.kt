package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.BrowseResponse
import com.github.innertube.models.GridRenderer
import com.github.innertube.models.MusicCarouselShelfRenderer
import com.github.innertube.models.MusicTwoRowItemRenderer
import com.github.innertube.models.NavigationEndpoint
import com.github.innertube.models.SectionListRenderer
import com.github.innertube.models.bodies.BrowseBody
import com.github.innertube.utils.from
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

// Pages of the YouTube Music "Explore" tab. Sections are identified by their structure
// rather than their titles, because the titles are translated.

private const val NEW_RELEASES_ALBUMS = "FEmusic_new_releases_albums"

private suspend fun Innertube.browseSections(
    browseId: String,
    country: String?,
    params: String? = null,
    formData: BrowseBody.FormData? = null
): List<SectionListRenderer.Content> = client.post(BROWSE) {
    setBody(BrowseBody(country = country, browseId = browseId, params = params, formData = formData))
}.body<BrowseResponse>()
    .contents
    ?.singleColumnBrowseResultsRenderer
    ?.tabs
    ?.firstOrNull()
    ?.tabRenderer
    ?.content
    ?.sectionListRenderer
    ?.contents
    .orEmpty()

private val SectionListRenderer.Content.twoRowItems: List<MusicTwoRowItemRenderer>
    get() = musicCarouselShelfRenderer
        ?.contents
        ?.mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
        ?: gridRenderer?.items?.mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
        .orEmpty()

/** Charts ranked by YouTube from what people actually watch and listen to in [country]. */
suspend fun Innertube.chartsPage(country: String) = runCatchingNonCancellable {
    val playlistCarousels = browseSections(
        browseId = "FEmusic_charts",
        country = country,
        formData = BrowseBody.FormData(selectedValues = listOf(country))
    ).map { section -> section.twoRowItems.mapNotNull(Innertube.PlaylistItem::from) }
        .filter { it.isNotEmpty() }

    Innertube.ChartsPage(
        charts = playlistCarousels.getOrNull(0).orEmpty(),
        languageCharts = playlistCarousels.getOrNull(1).orEmpty()
    )
}

/** Singles, EPs and albums, newest first as YouTube Music lists them. */
suspend fun Innertube.newReleases(country: String, all: Boolean = false) = runCatchingNonCancellable {
    val sections = browseSections(
        browseId = if (all) NEW_RELEASES_ALBUMS else "FEmusic_new_releases",
        country = country
    )

    val albumsSection = if (all) sections.firstOrNull()
    else sections.find { section ->
        section.musicCarouselShelfRenderer
            ?.header
            ?.musicCarouselShelfBasicHeaderRenderer
            ?.moreContentButton
            ?.buttonRenderer
            ?.navigationEndpoint
            ?.browseEndpoint
            ?.browseId == NEW_RELEASES_ALBUMS
    }

    albumsSection?.twoRowItems?.mapNotNull(::newReleaseAlbumItem).orEmpty()
}

// The subtitle reads "Single • Artist & Artist", unlike other album lists that end with the year
private fun newReleaseAlbumItem(renderer: MusicTwoRowItemRenderer): Innertube.AlbumItem? {
    val subtitleRuns = renderer.subtitle?.runs.orEmpty()

    return Innertube.AlbumItem(
        info = renderer.title?.runs?.firstOrNull()?.let(Innertube::Info),
        authors = subtitleRuns.drop(2)
            .map<_, Innertube.Info<NavigationEndpoint.Endpoint.Browse>>(Innertube::Info)
            .takeIf { it.isNotEmpty() },
        year = subtitleRuns.firstOrNull()?.text,
        thumbnail = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
    ).takeIf { it.info?.endpoint?.browseId != null }
}

suspend fun Innertube.moodsAndGenres(country: String) = runCatchingNonCancellable {
    val grids = browseSections(browseId = "FEmusic_moods_and_genres", country = country)
        .mapNotNull { section ->
            section.gridRenderer?.items?.mapNotNull { item ->
                val button = item.musicNavigationButtonRenderer ?: return@mapNotNull null
                Innertube.Mood(
                    title = button.buttonText?.text ?: return@mapNotNull null,
                    endpoint = button.clickCommand?.browseEndpoint ?: return@mapNotNull null,
                    stripeColor = button.solid?.leftStripeColor
                )
            }?.takeIf { it.isNotEmpty() }
        }

    Innertube.MoodsAndGenresPage(
        moods = grids.getOrNull(0).orEmpty(),
        genres = grids.getOrNull(1).orEmpty()
    )
}

/** The playlists YouTube Music groups under a mood or genre. */
suspend fun Innertube.moodPage(mood: Innertube.Mood, country: String) = runCatchingNonCancellable {
    browseSections(
        browseId = mood.endpoint.browseId ?: error("Mood without a browse id"),
        country = country,
        params = mood.endpoint.params
    ).mapNotNull { section ->
        val playlists = section.twoRowItems.mapNotNull(Innertube.PlaylistItem::from)
        if (playlists.isEmpty()) return@mapNotNull null

        Innertube.PlaylistsSection(
            title = section.musicCarouselShelfRenderer
                ?.header
                ?.musicCarouselShelfBasicHeaderRenderer
                ?.title
                ?.text,
            playlists = playlists
        )
    }
}
