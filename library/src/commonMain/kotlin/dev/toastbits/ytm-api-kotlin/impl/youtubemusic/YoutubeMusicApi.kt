package dev.toastbits.ytmapi.impl.youtubemusic

import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.YoutubeApi
import dev.toastbits.ytmapi.YoutubeApi.PostBodyContext
import dev.toastbits.ytmapi.endpoint.ArtistRadioEndpoint
import dev.toastbits.ytmapi.endpoint.ArtistShuffleEndpoint
import dev.toastbits.ytmapi.executeResult
import dev.toastbits.ytmapi.formats.VideoFormatsEndpoint
import dev.toastbits.ytmapi.formats.VideoFormatsEndpointType
import dev.toastbits.ytmapi.impl.youtubemusic.endpoint.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

internal val PLAIN_HEADERS = listOf("accept-language", "user-agent", "accept-encoding", "content-encoding", "origin")

class RelatedGroup(val title: String?, val items: List<MediaItem>?, val description: String?)

class YoutubeMusicApi(
    override val data_language: String,
    override val ui_language: String = data_language,
    override val user_auth_state: YoutubeMusicAuthInfo? = null,
    override val item_cache: MediaItemCache? = null,
    override val client: HttpClient = HttpClient(CIO),
    val api_url: String = YoutubeApi.Type.YOUTUBE_MUSIC.getDefaultUrl()
): YoutubeApi {
    init {
        check(!api_url.endsWith('/'))
        client.expectSuccess = true
    }

    private var initialised: Boolean = false

    private lateinit var youtubei_headers: Headers

    private lateinit var youtubei_context: JsonObject
    private lateinit var youtubei_context_android_music: JsonObject
    private lateinit var youtubei_context_android: JsonObject
    private lateinit var youtubei_context_mobile: JsonObject
    private lateinit var youtube_context_ui_language: JsonObject

    private fun buildHeaders() {
        val headers_builder: Headers.Builder = Headers.Builder()

        for (header in RequestData.ytm_headers) {
            headers_builder.add(header.key, header.value)
        }

        headers_builder["origin"] = api_url
        headers_builder["user-agent"] = getUserAgent()

        youtubei_headers = headers_builder    }

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
        val data_hl: String = data_language.split('-', limit = 2).firstOrNull()
        val ui_dl: String = ui_language.split('-', limit = 2).firstOrNull()

        youtubei_context = RequestData.getYtmContext(data_hl)
        youtubei_context_android_music = RequestData.getYtmContextAndroidMusic(data_hl)
        youtubei_context_android = RequestData.getYtmContextAndroid(data_hl)
        youtubei_context_mobile = RequestData.getYtmContextMo(data_hl)

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

        if (!include.isNullOrEmpty()) {
            for (header_key in include) {
                val value = youtubei_headers[header_key] ?: continue
                header(header_key, value)
            }
        }
        else {
            for (header in youtubei_headers) {
                header(header.first, header.second)
            }
        }

        return this
    }

    override fun HttpRequestBuilder.endpointPath(path: String): HttpRequestBuilder {
        path(api_url + path)
        url.parameters.append("prettyPrint", "false")
        return this
    }

    override suspend fun HttpRequestBuilder.postWithBody(body: Map<String, Any?>?, context: PostBodyContext): HttpRequestBuilder {
        val final_body: JsonObject = context.getContextPostBody().deepCopy()

        for (entry in body ?: emptyMap()) {
            final_body.remove(entry.key)
            final_body.add(entry.key, Json.decodeToJsonElement(entry.value))
        }
        return post(
            Json.encodeToString(final_body).toRequestBody("application/json".toMediaType())
        )
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
