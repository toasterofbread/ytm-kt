package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeCreateChannelResponse(val navigationEndpoint: ChannelNavigationEndpoint) {
    @Serializable
    data class ChannelNavigationEndpoint(val browseEndpoint: BrowseEndpoint)
}
