package com.github.innertube.requests

import com.github.innertube.Innertube
import com.github.innertube.models.MusicResponsiveListItemRenderer
import com.github.innertube.models.NavigationEndpoint
import com.github.innertube.models.SearchSuggestionsResponse
import com.github.innertube.models.bodies.SearchSuggestionsBody
import com.github.innertube.utils.runCatchingNonCancellable
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

suspend fun Innertube.searchSuggestions(input: String) = runCatchingNonCancellable {
    val response = client.post(SEARCH_SUGGESTIONS) {
        setBody(SearchSuggestionsBody(input = input))
        mask("contents.searchSuggestionsSectionRenderer.contents(searchSuggestionRenderer.navigationEndpoint.searchEndpoint.query,musicResponsiveListItemRenderer(flexColumns,thumbnail,navigationEndpoint))")
    }.body<SearchSuggestionsResponse>()

    // The queries and the top results come in separate sections
    val contents = response
        .contents
        ?.flatMap { it.searchSuggestionsSectionRenderer?.contents.orEmpty() }
        .orEmpty()

    Innertube.SearchSuggestions(
        queries = contents.mapNotNull { content ->
            content
                .searchSuggestionRenderer
                ?.navigationEndpoint
                ?.searchEndpoint
                ?.query
        },
        results = contents.mapNotNull { content ->
            content.musicResponsiveListItemRenderer?.let(::suggestedResult)
        }
    )
}

private fun suggestedResult(renderer: MusicResponsiveListItemRenderer): Innertube.SuggestedResult? {
    val title = renderer.flexColumns.getOrNull(0)
        ?.musicResponsiveListItemFlexColumnRenderer
        ?.text
        ?.runs
        ?.firstOrNull()
        ?.text
        ?: return null
    val subtitleRuns = renderer.flexColumns.getOrNull(1)
        ?.musicResponsiveListItemFlexColumnRenderer
        ?.text
    // "Type • Artists • Plays": the artists are the second part, if any
    val subtitleParts = subtitleRuns?.splitBySeparator().orEmpty()
    val authors = subtitleParts.getOrNull(1)
        ?.map<_, Innertube.Info<NavigationEndpoint.Endpoint.Browse>>(Innertube::Info)
    val thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
    val endpoint = renderer.navigationEndpoint

    val item = endpoint?.watchEndpoint?.let { watch ->
        val info = Innertube.Info(name = title, endpoint = watch)

        // Official audio tracks are songs; music and user videos are videos
        if (watch.type == "MUSIC_VIDEO_TYPE_ATV") Innertube.SongItem(
            info = info,
            authors = authors,
            album = null,
            durationText = null,
            thumbnail = thumbnail
        ) else Innertube.VideoItem(
            info = info,
            authors = authors?.takeIf { subtitleParts.size > 2 },
            viewsText = subtitleParts.lastOrNull()?.joinToString("") { it.text.orEmpty() },
            durationText = null,
            thumbnail = thumbnail
        )
    } ?: endpoint?.browseEndpoint?.let { browse ->
        val info = Innertube.Info(name = title, endpoint = browse)

        when (browse.type) {
            "MUSIC_PAGE_TYPE_ARTIST" -> Innertube.ArtistItem(
                info = info,
                subscribersCountText = subtitleRuns?.text,
                thumbnail = thumbnail
            )

            "MUSIC_PAGE_TYPE_ALBUM" -> Innertube.AlbumItem(
                info = info,
                authors = authors,
                year = subtitleParts.getOrNull(2)?.firstOrNull()?.text,
                thumbnail = thumbnail
            )

            "MUSIC_PAGE_TYPE_PLAYLIST" -> Innertube.PlaylistItem(
                info = info,
                channel = authors?.firstOrNull(),
                songCount = null,
                thumbnail = thumbnail
            )

            else -> null
        }
    } ?: return null

    return Innertube.SuggestedResult(item = item, subtitle = subtitleRuns?.text)
}
