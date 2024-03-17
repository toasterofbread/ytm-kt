package dev.toastbits.ytmkt.model.internal

import dev.toastbits.ytmkt.model.external.mediaitem.YtmArtist
import dev.toastbits.ytmkt.model.external.mediaitem.YtmMediaItem
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeAccountMenuResponse(val actions: List<Action>) {
    @Serializable
    data class Action(val openPopupAction: OpenPopupAction)
    @Serializable
    data class OpenPopupAction(val popup: Popup)
    @Serializable
    data class Popup(val multiPageMenuRenderer: MultiPageMenuRenderer)
    @Serializable
    data class MultiPageMenuRenderer(val sections: List<Section>, val header: Header?)

    @Serializable
    data class Section(val multiPageMenuSectionRenderer: MultiPageMenuSectionRenderer)
    @Serializable
    data class MultiPageMenuSectionRenderer(val items: List<Item>)
    @Serializable
    data class Item(val compactLinkRenderer: CompactLinkRenderer)
    @Serializable
    data class CompactLinkRenderer(val navigationEndpoint: NavigationEndpoint?)

    @Serializable
    data class Header(val activeAccountHeaderRenderer: ActiveAccountHeaderRenderer)
    @Serializable
    data class ActiveAccountHeaderRenderer(val accountName: TextRuns, val accountPhoto: MusicThumbnailRenderer.RendererThumbnail)

    fun getAritst(): YtmArtist? {
        val account: ActiveAccountHeaderRenderer =
            actions.first().openPopupAction.popup.multiPageMenuRenderer.header?.activeAccountHeaderRenderer
            ?: return null

        return YtmArtist(
            id = getChannelId() ?: return null,
            name = account.accountName.first_text,
            thumbnail_provider = ThumbnailProvider.fromThumbnails(account.accountPhoto.thumbnails)
        )
    }

    private fun getSections() = actions.first().openPopupAction.popup.multiPageMenuRenderer.sections

    private fun getChannelId(): String? {
        for (section in getSections()) {
            for (item in section.multiPageMenuSectionRenderer.items) {
                val browse_endpoint = item.compactLinkRenderer.navigationEndpoint?.browseEndpoint
                if (browse_endpoint?.getMediaItemType() == YtmMediaItem.Type.ARTIST) {
                    return browse_endpoint.browseId
                }
            }
        }
        return null
    }

    fun getChannelCreationToken(): String? {
        for (section in getSections()) {
            for (item in section.multiPageMenuSectionRenderer.items) {
                val endpoint = item.compactLinkRenderer.navigationEndpoint?.channelCreationFormEndpoint
                if (endpoint != null) {
                    return endpoint.channelCreationToken
                }
            }
        }
        return null
    }
}