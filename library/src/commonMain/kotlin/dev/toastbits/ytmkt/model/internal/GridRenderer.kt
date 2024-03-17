package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable

@Serializable
data class GridRenderer(val items: List<YoutubeiShelfContentsItem>, override val header: GridHeader?): YoutubeiHeaderContainer

@Serializable
data class GridHeader(val gridHeaderRenderer: HeaderRenderer): YoutubeiHeader {
    override val header_renderer: HeaderRenderer?
        get() = gridHeaderRenderer
}
