package dev.toastbits.ytmkt.model

import dev.toastbits.ytmkt.endpoint.AccountPlaylistAddSongsEndpoint
import dev.toastbits.ytmkt.endpoint.AccountPlaylistEditorEndpoint
import dev.toastbits.ytmkt.endpoint.AccountPlaylistsEndpoint
import dev.toastbits.ytmkt.endpoint.CreateAccountPlaylistEndpoint
import dev.toastbits.ytmkt.endpoint.DeleteAccountPlaylistEndpoint
import dev.toastbits.ytmkt.endpoint.LikedAlbumsEndpoint
import dev.toastbits.ytmkt.endpoint.LikedArtistsEndpoint
import dev.toastbits.ytmkt.endpoint.LikedPlaylistsEndpoint
import dev.toastbits.ytmkt.endpoint.MarkSongAsWatchedEndpoint
import dev.toastbits.ytmkt.endpoint.SetSongLikedEndpoint
import dev.toastbits.ytmkt.endpoint.SetSubscribedToArtistEndpoint
import dev.toastbits.ytmkt.endpoint.SongLikedEndpoint
import dev.toastbits.ytmkt.endpoint.SubscribedToArtistEndpoint
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder

abstract class ApiAuthenticationState(
    headers: Headers,
    headers_to_include: List<String>? = null
) {
    abstract val api: YtmApi
    abstract val own_channel_id: String?
    val headers: Headers

    init {
        val headers_builder: HeadersBuilder = HeadersBuilder()

        for ((key, values) in headers.entries()) {
            if (headers_to_include?.none { it.equals(key, ignoreCase = true) } == true) {
                continue
            }

            if (key.equals("cookie", ignoreCase = true)) {
                headers_builder.appendAll(
                    key,
                    values.map {
                        filterCookieString(it) {
                            it.startsWith("__Secure-")
                        }
                    }
                )
            }
            else {
                headers_builder.appendAll(key, values)
            }
        }

        this.headers = headers_builder.build()
    }

    private fun filterCookieString(cookies_string: String, shouldKeepCookie: (String) -> Boolean): String {
        var ret: String = ""
        val cookies: List<String> = cookies_string.split(';')
            .mapNotNull {
                it.trim().takeIf { it.isNotEmpty() }
            }

        for (cookie in cookies) {
            val (name, value) = cookie.split('=', limit = 2)

            if (!shouldKeepCookie(name)) {
                continue
            }

            ret += "$name=$value;"
        }

        return ret
    }

    fun addHeadersToRequest(builder: HttpRequestBuilder, include: List<String>? = null) {
        builder.headers {
            if (!include.isNullOrEmpty()) {
                for (header_key in include) {
                    val value = headers[header_key] ?: continue
                    set(header_key, value)
                }
            }
            else {
                for ((key, value) in headers.entries()) {
                    set(key, value.first())
                }
            }
        }
    }

    // --- Account playlists ---
    abstract val AccountPlaylists: AccountPlaylistsEndpoint
    abstract val CreateAccountPlaylist: CreateAccountPlaylistEndpoint
    abstract val DeleteAccountPlaylist: DeleteAccountPlaylistEndpoint
    abstract val AccountPlaylistEditor: AccountPlaylistEditorEndpoint
    abstract val AccountPlaylistAddSongs: AccountPlaylistAddSongsEndpoint

    // --- Account liked items ---
    abstract val LikedAlbums: LikedAlbumsEndpoint
    abstract val LikedArtists: LikedArtistsEndpoint
    abstract val LikedPlaylists: LikedPlaylistsEndpoint

    // --- Interaction ---
    abstract val SubscribedToArtist: SubscribedToArtistEndpoint
    abstract val SetSubscribedToArtist: SetSubscribedToArtistEndpoint
    abstract val SongLiked: SongLikedEndpoint
    abstract val SetSongLiked: SetSongLikedEndpoint
    abstract val MarkSongAsWatched: MarkSongAsWatchedEndpoint

    companion object
}