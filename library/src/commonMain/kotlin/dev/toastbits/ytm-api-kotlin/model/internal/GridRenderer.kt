package dev.toastbits.ytmapi.model.internal

data class GridRenderer(val items: List<YoutubeiShelfContentsItem>, override val header: GridHeader?): YoutubeiHeaderContainer
data class GridHeader(val gridHeaderRenderer: HeaderRenderer): YoutubeiHeader {
    override val header_renderer: HeaderRenderer?
        get() = gridHeaderRenderer
}
