package dev.toastbits.ytmkt.model.internal

data class YoutubeCreateChannelResponse(val navigationEndpoint: ChannelNavigationEndpoint) {
    data class ChannelNavigationEndpoint(val browseEndpoint: BrowseEndpoint)
}
