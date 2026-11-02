package com.zetroit.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zetroit.music.database
import com.zetroit.music.enums.PlaylistSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.PlaylistPreview

class HomePlaylistsViewModel : ViewModel() {
    var items: List<PlaylistPreview> by mutableStateOf(emptyList())

    suspend fun loadArtists(
        sortBy: PlaylistSortBy,
        sortOrder: SortOrder
    ) {
        database
            .playlistPreviews(sortBy, sortOrder)
            .collect { items = it }
    }
}