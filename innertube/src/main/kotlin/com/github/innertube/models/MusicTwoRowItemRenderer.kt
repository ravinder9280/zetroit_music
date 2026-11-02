package com.github.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val navigationEndpoint: NavigationEndpoint?,
    val thumbnailRenderer: ThumbnailRenderer?,
    val title: Runs?,
    val subtitle: Runs?,
    val thumbnailOverlay: ThumbnailOverlay? = null
) {
    @Serializable
    data class ThumbnailOverlay(
        val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer?
    ) {
        @Serializable
        data class MusicItemThumbnailOverlayRenderer(
            val content: Content?
        ) {
            @Serializable
            data class Content(
                val musicPlayButtonRenderer: MusicPlayButtonRenderer?
            ) {
                @Serializable
                data class MusicPlayButtonRenderer(
                    val playNavigationEndpoint: NavigationEndpoint?
                )
            }
        }
    }

    /** What the play button on the thumbnail plays, when the item itself opens a page. */
    val playEndpoint: NavigationEndpoint.Endpoint.Watch?
        get() = thumbnailOverlay
            ?.musicItemThumbnailOverlayRenderer
            ?.content
            ?.musicPlayButtonRenderer
            ?.playNavigationEndpoint
            ?.watchEndpoint
}
