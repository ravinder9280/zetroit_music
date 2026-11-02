package com.zetroit.music.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.github.innertube.Innertube

class ItemsPageViewModel<T : Innertube.Item> : ViewModel() {
    var itemsMap: MutableMap<String, Innertube.ItemsPage<T>?> = mutableStateMapOf()

    // Callers pass the already merged page; the result of plus used to be discarded here,
    // so pages after the first never showed up
    fun setItems(
        tag: String,
        items: Innertube.ItemsPage<T>?
    ) {
        itemsMap[tag] = items
    }
}