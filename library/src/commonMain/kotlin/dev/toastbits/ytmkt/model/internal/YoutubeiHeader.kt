package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.radio.YoutubeiNextResponse
import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val musicCarouselShelfBasicHeaderRenderer: HeaderRenderer?,
    val musicImmersiveHeaderRenderer: HeaderRenderer?,
    val musicVisualHeaderRenderer: HeaderRenderer?,
    val musicDetailHeaderRenderer: MusicDetailHeaderRenderer?,
    val musicEditablePlaylistDetailHeaderRenderer: MusicEditablePlaylistDetailHeaderRenderer?,
    val musicCardShelfHeaderBasicRenderer: HeaderRenderer?
): YoutubeiHeader {
    fun getRenderer(): HeaderRenderer? {
        return musicCarouselShelfBasicHeaderRenderer
            ?: musicImmersiveHeaderRenderer
            ?: musicVisualHeaderRenderer
            ?: musicDetailHeaderRenderer?.toHeaderRenderer()
            ?: musicCardShelfHeaderBasicRenderer
            ?: musicEditablePlaylistDetailHeaderRenderer?.header?.getRenderer()
    }

    @Serializable
    data class MusicEditablePlaylistDetailHeaderRenderer(val header: Header)

    @Serializable
    data class MusicDetailHeaderRenderer(
        val menu: Menu,
        val title: TextRuns? = null,
        val strapline: TextRuns? = null,
        val subscriptionButton: SubscriptionButton? = null,
        val playButton: MoreContentButton? = null,
        val description: TextRuns? = null,
        val thumbnail: Thumbnails? = null,
        val foregroundThumbnail: Thumbnails? = null,
        val subtitle: TextRuns? = null,
        val secondSubtitle: TextRuns? = null,
        val moreContentButton: MoreContentButton? = null
    ) {
        fun toHeaderRenderer(): HeaderRenderer =
            HeaderRenderer(
                title,
                strapline,
                subscriptionButton,
                playButton,
                description,
                thumbnail,
                foregroundThumbnail,
                subtitle,
                secondSubtitle,
                moreContentButton
            )
    }
    @Serializable
    data class Menu(val menuRenderer: MenuRenderer)
    @Serializable
    data class MenuRenderer(val topLevelButtons: List<TopLevelButton>? = null)
    @Serializable
    data class TopLevelButton(val buttonRenderer: TopLevelButtonRenderer?)
    @Serializable
    data class TopLevelButtonRenderer(val icon: YoutubeiNextResponse.MenuIcon? = null)

    override val header_renderer: HeaderRenderer?
        get() = getRenderer()
}

interface YoutubeiHeaderContainer {
    val header: YoutubeiHeader?
}
interface YoutubeiHeader {
    val header_renderer: HeaderRenderer?
}
