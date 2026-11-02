package com.zetroit.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zetroit.music.database
import com.zetroit.music.enums.AlbumSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.Album

class HomeAlbumsViewModel : ViewModel() {
    var items: List<Album> by mutableStateOf(emptyList())

    suspend fun loadAlbums(
        sortBy: AlbumSortBy,
        sortOrder: SortOrder
    ) {
        database
            .albums(sortBy, sortOrder)
            .collect { items = it }
    }
}