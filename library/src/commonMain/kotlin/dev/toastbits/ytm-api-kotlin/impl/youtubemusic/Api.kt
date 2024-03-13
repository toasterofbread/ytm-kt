package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.getReader
import okio.Buffer

const val DEFAULT_CONNECT_TIMEOUT = 10000
val REQUEST_HEADERS_TO_REMOVE = listOf("authorization", "cookie")

fun Request.stringify() = buildString {
    append("Request{method=")
    append(method)
    append(", url=")
    append(url)

    val include_headers: List<Pair<String, String>> = headers.filter { !REQUEST_HEADERS_TO_REMOVE.contains(it.first.lowercase()) }
    if (include_headers.isNotEmpty()) {
        append(", headers=[")
        include_headers.forEachIndexed { index, (name, value) ->
            if (index > 0) {
                append(", ")
            }
            append(name)
            append(':')
            append(value)
        }
        append(']')
    }
    append('}')
}

inline fun <I, O> Result<I>.cast(transform: (I) -> O = { it as O }): Result<O> {
    return fold(
        { runCatching { transform(it) } },
        { Result.failure(it) }
    )
}

fun <T> Result<T>.unit(): Result<Unit> {
    return fold(
        { Result.success(Unit) },
        { Result.failure(it) }
    )
}

fun <T> Result<T>.getOrThrowHere(): T =
    fold(
        { it },
        { throw Exception(it) }
    )

fun Artist.isOwnChannel(api: YoutubeApi): Boolean {
    return id == api.user_auth_state?.own_channel?.id
}
