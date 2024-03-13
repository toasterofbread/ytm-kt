package dev.toastbits.ytmapi.impl.youtubemusic
import com.toasterofbread.spmp.ui.layout.youtubemusiclogin.AccountSwitcherEndpoint
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicApi
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeMusicAuthInfo
import dev.toastbits.ytmapi.model.external.mediaitem.artist.ArtistRef
import com.toasterofbread.spmp.ui.layout.youtubemusiclogin.YTAccountMenuResponse
import dev.toastbits.ytmapi.impl.youtubemusic.YoutubeChannelNotCreatedException
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.executeResult
import dev.toastbits.ytmapi.fromJson
import io.ktor.client.statement.HttpResponse

internal object YTMLogin {
    fun replaceCookiesInString(base_cookies: String, new_cookies: List<String>): String {
        var cookie_string: String = base_cookies

        for (cookie in new_cookies) {
            val split: List<String> = cookie.split('=', limit = 2)

            val name: String = split[0]
            val new_value: String = split[1].split(';', limit = 2)[0]

            val cookie_start: Int = cookie_string.indexOf("$name=") + name.length + 1
            if (cookie_start != -1) {
                val cookie_end: Int = cookie_string.indexOf(';', cookie_start)
                cookie_string = (
                    cookie_string.substring(0, cookie_start)
                    + new_value
                    + if (cookie_end != -1) cookie_string.substring(cookie_end, cookie_string.length) else ""
                )
            }
            else {
                cookie_string += "; $name=$new_value"
            }
        }

        return cookie_string
    }

    suspend fun completeLoginWithAccount(
        headers: Headers,
        account: AccountSwitcherEndpoint.AccountItem,
        api: YoutubeMusicApi
    ): Result<YoutubeMusicAuthInfo> = runCatching {
        val account_headers: Headers

        if (!account.isSelected) {
            val sign_in_url: String =
                account.serviceEndpoint.selectActiveIdentityEndpoint.supportedTokens.first { it.accountSigninToken != null }.accountSigninToken!!.signinUrl

            val response: HttpResponse = api.client.request {
                endpointPath(sign_in_url)
                headers(headers)
            }

            val new_cookies: List<String> =
                response.headers.entries.mapNotNull { header ->
                    if (header.key.lowercase() == "Set-Cookie") header.value
                    else null
                }

            val cookie_string: String = replaceCookiesInString(
                headers["Cookie"]!!,
                new_cookies
            )

            account_headers = headers
                .newBuilder()
                .set("Cookie", cookie_string)

            val channel_id: String? =
                account.serviceEndpoint.selectActiveIdentityEndpoint.supportedTokens.firstOrNull { it.offlineCacheKeyToken != null }?.offlineCacheKeyToken?.clientCacheKey

            if (channel_id != null) {
                return@runCatching YoutubeMusicAuthInfo.create(api, ArtistRef("UC$channel_id"), account_headers)
            }
        }
        else {
            account_headers = headers
        }

        return@runCatching completeLogin(account_headers, api).getOrThrow()
    }

    suspend fun completeLogin(
        headers: Headers,
        api: YoutubeMusicApi
    ): Result<YoutubeMusicAuthInfo> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("account/account_menu")
            headers(headers)
            addAuthlessApiHeaders()
            postWithBody()
        }

        val account_menu: YTAccountMenuResponse = response.body

        val headers_builder: Headers.Builder = headers.newBuilder()
        val new_cookies: List<String> = response.headers.mapNotNull { header ->
            if (header.first == "Set-Cookie") header.second
            else null
        }
        headers_builder["Cookie"] = replaceCookiesInString(headers_builder["Cookie"]!!, new_cookies)

        val channel: Artist? = account_menu.getAritst()
        if (channel == null) {
            throw YoutubeChannelNotCreatedException(headers_builder.build(), account_menu.getChannelCreationToken())
        }

        return@runCatching YoutubeMusicAuthInfo.create(api, channel, headers_builder.build())
    }
}
