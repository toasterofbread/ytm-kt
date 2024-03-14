package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.YoutubeApi.PostBodyContext
import dev.toastbits.ytmapi.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmapi.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmapi.formats.VideoFormatsEndpoint
import dev.toastbits.ytmapi.formats.VideoFormatsEndpointType
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.*
import dev.toastbits.ytmapi.itemcache.MediaItemCache
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.HeadersBuilder
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.ContentType

internal val PLAIN_HEADERS = listOf("accept-language", "user-agent", "accept-encoding", "content-encoding", "origin")

class RelatedGroup(val title: String?, val items: List<MediaItem>?, val description: String?)

class YoutubeMusicApi(
    override val data_language: String,
    override val ui_language: String = data_language,
    override val user_auth_state: YoutubeMusicAuthInfo? = null,
    override val item_cache: MediaItemCache = MediaItemCache(),
    val api_url: String = YoutubeApi.Type.YOUTUBE_MUSIC.getDefaultUrl(),
    configureClient: HttpClientConfig<*>.() -> Unit = {}
): YoutubeApi {
    override val client: HttpClient = HttpClient(CIO) {
        configureClient()
        expectSuccess = true
    }

    init {
        check(!api_url.endsWith('/'))
    }

    private var initialised: Boolean = false

    private lateinit var youtubei_headers: Headers

    private lateinit var youtubei_context: JsonObject
    private lateinit var youtubei_context_android_music: JsonObject
    private lateinit var youtubei_context_android: JsonObject
    private lateinit var youtubei_context_mobile: JsonObject
    private lateinit var youtube_context_ui_language: JsonObject

    private fun buildHeaders() {
        val headers_builder: HeadersBuilder = HeadersBuilder()

        for ((key, value) in RequestData.ytm_headers) {
            headers_builder.append(key, value)
        }

        headers_builder.set("origin", api_url)
        headers_builder.set("user-agent", getUserAgent())

        youtubei_headers = headers_builder.build()
    }

    private val init_lock = Mutex()
    override suspend fun init() {
        init_lock.withLock {
            if (initialised) {
                return
            }

            coroutineScope {
                launch(Dispatchers.Default) {
                    launch {
                        buildHeaders()
                    }
                    launch {
                        updateYtmContext()
                    }
                }
            }

            initialised = true
        }
    }

    private fun updateYtmContext() {
        val data_hl: String = data_language.split('-', limit = 2).first()
        val ui_hl: String = ui_language.split('-', limit = 2).first()

        youtubei_context = RequestData.getYtmContext(data_hl)
        youtubei_context_android_music = RequestData.getYtmContextAndroidMusic(data_hl)
        youtubei_context_android = RequestData.getYtmContextAndroid(data_hl)
        youtubei_context_mobile = RequestData.getYtmContextMobile(data_hl)

        youtube_context_ui_language = RequestData.getYtmContext(ui_hl)
    }

    fun getUserAgent(): String = RequestData.ytm_user_agent

    override suspend fun PostBodyContext.getContextPostBody(): JsonObject {
        init()
        return when (this) {
            PostBodyContext.BASE -> youtubei_context
            PostBodyContext.ANDROID_MUSIC -> youtubei_context_android_music
            PostBodyContext.ANDROID -> youtubei_context_android
            PostBodyContext.MOBILE -> youtubei_context_mobile
            PostBodyContext.UI_LANGUAGE -> youtube_context_ui_language
        }
    }

    override suspend fun HttpRequestBuilder.addAuthlessApiHeaders(include: List<String>?): HttpRequestBuilder {
        init()

        headers {
            if (!include.isNullOrEmpty()) {
                for (header_key in include) {
                    val value: String = youtubei_headers.get(header_key) ?: continue
                    set(header_key, value)
                }
            }
            else {
                for ((key, value) in youtubei_headers.entries()) {
                    set(key, value.first())
                }
            }
        }

        return this
    }

    override fun HttpRequestBuilder.endpointPath(path: String): HttpRequestBuilder {
        check(!path.contains("?")) { path }

        url.pathSegments = (api_url + path).split("/")
        url.parameters.append("prettyPrint", "false")
        return this
    }

    override suspend fun HttpRequestBuilder.postWithBody(
        context: PostBodyContext,
        buildPostBody: (JsonObjectBuilder.() -> Unit)?
    ): HttpRequestBuilder {
        contentType(ContentType.Application.Json)

        val context_body: JsonObject = context.getContextPostBody()
        if (buildPostBody == null) {
            setBody(context_body)
        }
        else {
            val body: MutableMap<String, JsonElement> = buildJsonObject(buildPostBody).toMutableMap()
            for ((key, value) in context_body) {
                check(!body.containsKey(key)) {
                    "Post body from endpoint contains key $key, which conflicts with the context body"
                }

                body[key] = value
            }
            setBody(body)
        }

        return this
    }

    override val UpdateUserAuthState = YTMUserAuthStateEndpoint(this)
    override val YoutubeChannelCreationForm = YTMYoutubeChannelCreationFormEndpoint(this)
    override val CreateYoutubeChannel = YTMCreateYoutubeChannelEndpoint(this)

    override val LoadSong = YTMLoadSongEndpoint(this)
    override val LoadArtist = YTMLoadArtistEndpoint(this)
    override val LoadPlaylist = YTMLoadPlaylistEndpoint(this)

    override val VideoFormats: VideoFormatsEndpoint = TODO()

    override val HomeFeed = YTMGetHomeFeedEndpoint(this)
    override val GenericFeedViewMorePage = YTMGenericFeedViewMorePageEndpoint(this)
    override val SongRadio = YTMSongRadioEndpoint(this)

    override val ArtistWithParams = YTMArtistWithParamsEndpoint(this)
    override val ArtistRadio: ArtistRadioEndpoint = YTMArtistRadioEndpoint(this)
    override val ArtistShuffle: ArtistShuffleEndpoint = YTMArtistShuffleEndpoint(this)

    override val PlaylistContinuation = YTMPlaylistContinuationEndpoint(this)

    override val RadioBuilder = YTMRadioBuilderEndpoint(this)

    override val SongRelatedContent = YTMSongRelatedContentEndpoint(this)
    override val SongLyrics = YTMSongLyricsEndpoint(this)

    override val Search = YTMSearchEndpoint(this)
    override val SearchSuggestions = YTMSearchSuggestionsEndpoint(this)
}
