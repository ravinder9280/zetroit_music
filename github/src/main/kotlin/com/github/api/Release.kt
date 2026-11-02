package com.github.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val draft: Boolean,
    val name: String,
    // Like "v2.0.0"
    @SerialName("tag_name") val tagName: String,
    val prerelease: Boolean
)