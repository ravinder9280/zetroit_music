package com.zetroit.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zetroit.music.database
import com.zetroit.music.enums.ArtistSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.Artist

class HomeArtistsViewModel : ViewModel() {
    var items: List<Artist> by mutableStateOf(emptyList())

    suspend fun loadArtists(
        sortBy: ArtistSortBy,
        sortOrder: SortOrder
    ) {
        database
            .artists(sortBy, sortOrder)
            .collect { items = it }
    }
}