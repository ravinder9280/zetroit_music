package com.github.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: Runs?,
    val solid: Solid?,
    val clickCommand: NavigationEndpoint?
) {
    @Serializable
    data class Solid(
        val leftStripeColor: Long?
    )
}
