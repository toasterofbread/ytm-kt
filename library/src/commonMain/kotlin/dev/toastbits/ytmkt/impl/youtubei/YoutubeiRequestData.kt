package dev.toastbits.ytmkt.impl.youtubei

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority

object YoutubeiRequestData {
    val default_hl: String = "en-GB"
    val ytm_user_agent: String = "Mozilla/5.0 (X11; Linux x86_64; rv:105.0) Gecko/20100101 Firefox/105.0"

    fun getYtmHeaders(origin_url: String): Map<String, String> {
        val root_origin_url: String = Url(origin_url).protocolWithAuthority
        return mapOf(
            "accept" to "*/*",
            "content-type" to "application/json",
            "x-youtube-client-name" to "67",
            "x-youtube-client-version" to "1.20221019.01.00",
            "x-goog-authuser" to "0",
            "x-origin" to root_origin_url,
            "origin" to root_origin_url,
            "user-agent" to ytm_user_agent
        )
    }

    fun getYtmContext(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("platform", "DESKTOP")
                put("clientName", "WEB_REMIX")
                put("clientVersion", "1.20230306.01.00")
                put("userAgent", ytm_user_agent)
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }

    fun getYtmContextAndroid(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("platform", "MOBILE")
                put("clientName", "Android")
                put("clientVersion", "17.10.35")
                put("userAgent", ytm_user_agent)
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }

    fun getYtmContextMobile(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("platform", "MOBILE")
                put("clientName", "WEB_REMIX")
                put("clientVersion", "1.20230503.01.00")
                put("osName", "Android")
                put("osVersion", "12")
                put("userAgent", "Mozilla/5.0 (Linux; Android 12; Pixel 3a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.74 Mobile Safari/537.36,gzip(gfe)")
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }

    fun getYtmContextAndroidMusic(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("platform", "MOBILE")
                put("clientName", "ANDROID_MUSIC")
                put("clientVersion", "5.28.1")
                put("userAgent", "com.google.android.apps.youtube.music/5.28.1 (Linux; U; Android 11) gzip")
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }

    fun getYtmContextWeb(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("platform", "DESKTOP")
                put("clientName", "WEB")
                put("clientVersion", "2.20240509.00.00")
                put("userAgent", ytm_user_agent)
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }

    fun getYtmContextIos(hl: String): JsonObject = buildJsonObject {
        putJsonObject("context") {
            putJsonObject("client") {
                put("hl", hl)
                put("clientName", "IOS")
                put("clientVersion", "19.29.1")
                put("deviceMake", "Apple")
                put("deviceModel", "iPhone16,2")
                put("osName", "iPhone")
                put("osVersion", "17.5.1.21F90")
                put("userAgent", "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X;)")
                put("acceptHeader", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            }
            putJsonObject("user") {}
        }
    }
}
