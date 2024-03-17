package dev.toastbits.ytmkt.impl.youtubei

import kotlinx.serialization.json.JsonObject

enum class YoutubeiPostBody {
    BASE,
    ANDROID_MUSIC,
    ANDROID,
    MOBILE;

    fun getPostBody(api: YoutubeiApi): JsonObject =
        when (this) {
            BASE -> api.post_body_default
            ANDROID_MUSIC -> api.post_body_android_music
            ANDROID -> api.post_body_android
            MOBILE -> api.post_body_mobile
        }

    companion object {
        val DEFAULT: YoutubeiPostBody = BASE
    }
}
