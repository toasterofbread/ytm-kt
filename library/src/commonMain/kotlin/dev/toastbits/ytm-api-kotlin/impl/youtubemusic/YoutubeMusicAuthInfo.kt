package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.endpoint.GenericFeedViewMorePageEndpoint
import dev.toastbits.ytmapi.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmapi.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmapi.endpoint.LikedPlaylistsEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.*
import dev.toastbits.ytmapi.model.YoutubeiBrowseResponse

class YoutubeChannelNotCreatedException(
    val headers: Headers,
    val channel_creation_token: String?
): RuntimeException()

class YoutubeMusicAuthInfo private constructor(
    override val api: YoutubeMusicApi,
    override val own_channel_id: String,
    headers: Map<String, String>
): YoutubeApi.UserAuthState(headers) {
    companion object {
        val REQUIRED_HEADERS: List<String> = listOf("authorization", "cookie")

        fun create(api: YoutubeMusicApi, own_channel: Artist, headers: Headers): YoutubeMusicAuthInfo {
            own_channel.createDbEntry(api.database)
            return YoutubeMusicAuthInfo(api, own_channel, headers)
        }
    }

    override val AccountPlaylists = YTMAccountPlaylistsEndpoint(this)
    override val CreateAccountPlaylist = YTMCreateAccountPlaylistEndpoint(this)
    override val DeleteAccountPlaylist = YTMDeleteAccountPlaylistEndpoint(this)
    override val SubscribedToArtist = YTMSubscribedToArtistEndpoint(this)
    override val SetSubscribedToArtist = YTMSetSubscribedToArtistEndpoint(this)
    override val SongLiked = YTMSongLikedEndpoint(this)
    override val SetSongLiked = YTMSetSongLikedEndpoint(this)
    override val MarkSongAsWatched = YTMMarkSongAsWatchedEndpoint(this)
    override val AccountPlaylistEditor = YTMAccountPlaylistEditorEndpoint(this)
    override val AccountPlaylistAddSongs = YTMAccountPlaylistAddSongsEndpoint(this)
    override val LikedAlbums = YTMLikedAlbumsEndpoint(this)
    override val LikedArtists = YTMLikedArtistsEndpoint(this)
    override val LikedPlaylists = YTMLikedPlaylistsEndpoint(this)
}

class YTMGenericFeedViewMorePageEndpoint(override val api: YoutubeApi): GenericFeedViewMorePageEndpoint() {
    override suspend fun getGenericFeedViewMorePage(
        browse_id: String
    ): Result<List<MediaItem>> = runCatching {
        val hl: String = api.data_language
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addAuthApiHeaders()
            postWithBody(
                mapOf("browseId" to browse_id)
            )
        }

        val data: YoutubeiBrowseResponse = response.body

        val items: List<MediaItemData> = 
            data.contents!!
                .singleColumnBrowseResultsRenderer!!
                .tabs
                .first()
                .tabRenderer
                .content!!
                .sectionListRenderer!!
                .contents!!
                .first()
                .getMediaItems(hl)

        api.database.transaction {
            for (item in items) {
                item.saveToDatabase(api.database)
            }
        }

        return@runCatching items
    }
}
