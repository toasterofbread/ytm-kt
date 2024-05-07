package dev.toastbits.sample

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiAuthenticationState
import dev.toastbits.ytmkt.endpoint.SongFeedEndpoint
import dev.toastbits.ytmkt.endpoint.SongFeedLoadResult
import io.ktor.http.Headers
import io.ktor.client.request.request
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    // Initialise the Youtubei api implementation
    val api: YoutubeiApi =
        YoutubeiApi(
            data_language = "en-GB"
        )

    // Optionally, get and use a visitor ID so that continuations work while logged out
    // api.visitor_id = api.GetVisitorId.getVisitorId().getOrThrow()

    // Uncomment and populate Headers.build to use the API as a logged-in user
    // api.user_auth_state =
    //     YoutubeiAuthenticationState(
    //         api = api,
    //         own_channel_id = null,
    //         headers = Headers.build {
    //             append("Authorization", "")
    //             append("Cookie", "")
    //         }
    //     )

    // Get our API's implementation of the SongFeedEndpoint endpoint
    val endpoint: SongFeedEndpoint = api.SongFeed

    // Download the home page recommendations feed
    val song_feed: SongFeedLoadResult =
        endpoint.getSongFeed().getOrThrow()

    song_feed.printFeed()

    if (song_feed.ctoken == null) {
        println("Feed has no continuation")
        return@runBlocking
    }

    // Get the feed's continuation using the ctoken from the first result
    val song_feed_continuation: SongFeedLoadResult =
        endpoint.getSongFeed(continuation = song_feed.ctoken).getOrThrow()

    song_feed_continuation.printFeed()
}

fun SongFeedLoadResult.printFeed() {
    for (layout in layouts) {
        val en_title: String = layout.title?.getString("en-GB") ?: "?"
        val ja_title: String = layout.title?.getString("ja-JP") ?: "?"

        println("Layout $en_title / $ja_title has ${layout.items.size} items")
    }
}
