package dev.toastbits.ytmapi.model.internal

import dev.toastbits.ytmapi.model.external.Thumbnail
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.ThumbnailProvider

data class YoutubeAccountMenuResponse(val actions: List<Action>) {
    data class Action(val openPopupAction: OpenPopupAction)
    data class OpenPopupAction(val popup: Popup)
    data class Popup(val multiPageMenuRenderer: MultiPageMenuRenderer)
    data class MultiPageMenuRenderer(val sections: List<Section>, val header: Header?)

    data class Section(val multiPageMenuSectionRenderer: MultiPageMenuSectionRenderer)
    data class MultiPageMenuSectionRenderer(val items: List<Item>)
    data class Item(val compactLinkRenderer: CompactLinkRenderer)
    data class CompactLinkRenderer(val navigationEndpoint: NavigationEndpoint?)

    data class Header(val activeAccountHeaderRenderer: ActiveAccountHeaderRenderer)
    data class ActiveAccountHeaderRenderer(val accountName: TextRuns, val accountPhoto: MusicThumbnailRenderer.RendererThumbnail)

    fun getAritst(): Artist? {
        val account: ActiveAccountHeaderRenderer =
            actions.first().openPopupAction.popup.multiPageMenuRenderer.header?.activeAccountHeaderRenderer
            ?: return null

        return Artist(
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
                if (browse_endpoint?.getMediaItemType() == MediaItem.Type.ARTIST) {
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