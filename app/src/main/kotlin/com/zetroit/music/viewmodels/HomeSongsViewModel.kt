package com.zetroit.music.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.zetroit.music.database
import com.zetroit.music.enums.SongSortBy
import com.zetroit.music.enums.SortOrder
import com.zetroit.music.models.Song

class HomeSongsViewModel : ViewModel() {
    var items: List<Song> by mutableStateOf(emptyList())

    suspend fun loadSongs(
        sortBy: SongSortBy,
        sortOrder: SortOrder
    ) {
        database
            .songs(sortBy, sortOrder)
            .collect { items = it }
    }
}