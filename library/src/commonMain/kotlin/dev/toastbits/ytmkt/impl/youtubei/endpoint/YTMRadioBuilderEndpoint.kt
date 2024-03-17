package dev.toastbits.ytmkt.impl.youtubei.endpoint

import dev.toastbits.ytmkt.impl.youtubei.YoutubeiPostBody
import dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist
import dev.toastbits.ytmkt.model.external.Thumbnail
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.endpoint.RadioBuilderArtist
import dev.toastbits.ytmkt.endpoint.RadioBuilderEndpoint
import dev.toastbits.ytmkt.endpoint.RadioBuilderModifier
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.put

open class YTMRadioBuilderEndpoint(override val api: YoutubeiApi): RadioBuilderEndpoint() {
    // https://gist.github.com/toasterofbread/8982ffebfca5919cb51e8967e0122982
    override suspend fun getRadioBuilderArtists(
        selectThumbnail: (List<Thumbnail>) -> Thumbnail
    ): Result<List<RadioBuilderArtist>> = runCatching {
        val response: HttpResponse = api.client.request {
            endpointPath("browse")
            addApiHeadersWithAuthenticated()
            postWithBody(YoutubeiPostBody.ANDROID.getPostBody(api)) {
                put("browseId", "FEmusic_radio_builder")
            }
        }

        val parsed: RadioBuilderBrowseResponse = response.body()

        return@runCatching parsed.items.zip(parsed.mutations).mapNotNull { artist ->
            artist.second.token?.let { token ->
                RadioBuilderArtist(artist.first.title, token, selectThumbnail(artist.first.musicThumbnail.image.sources))
            }
        }
    }

    override fun buildRadioToken(artists: Set<RadioBuilderArtist>, modifiers: Set<RadioBuilderModifier?>): String {
        require(artists.isNotEmpty())
        var radio_token: String = "VLRDAT"

        var modifier_added: Boolean = false
        for (modifier in listOf(
            modifiers.singleOrNull { it is RadioBuilderModifier.FilterB },
            modifiers.singleOrNull { it is RadioBuilderModifier.FilterA },
            modifiers.singleOrNull { it is RadioBuilderModifier.SelectionType },
            modifiers.singleOrNull { it is RadioBuilderModifier.Variety }
        )) {
            modifier?.string?.also {
                radio_token += it
                modifier_added = true
            }
        }

        for (artist in artists.withIndex()) {
            val formatted_token = artist.value.token.removePrefix("RDAT")
                .let { token ->
                    if (token.first() == 'a' && artist.index != 0) {
                        'I' + token.substring(1)
                    } else token
                }
                .let { token ->
                    if (artists.size == 1 && !modifier_added) {
                        token
                    }
                    else if (artist.index + 1 == artists.size) {
                        token.take(token.lastIndexOf('E') + 1)
                    }
                    else {
                        token.take(token.lastIndexOf('E'))
                    }
                }

            radio_token += formatted_token
        }

        return radio_token
    }

    override suspend fun getBuiltRadio(radio_token: String): Result<YtmPlaylist?> = runCatching {
        require(radio_token.startsWith("VLRDAT"))
        require(radio_token.contains('E'))

        val playlist: YtmPlaylist = api.LoadPlaylist.loadPlaylist(radio_token).getOrThrow()

        val thumb_url: String? = playlist.thumbnail_provider?.getThumbnailUrl(ThumbnailProvider.Quality.HIGH)
        if (thumb_url?.contains("fallback") == true) {
            return@runCatching null
        }

        return@runCatching playlist.copy(type = YtmPlaylist.Type.RADIO)
    }
}

private data class RadioBuilderBrowseResponse(
    val contents: Contents,
    val frameworkUpdates: FrameworkUpdates
) {
    val items: List<SeedItem> get() =
        contents.singleColumnBrowseResultsRenderer.tabs.first().tabRenderer.content
            .sectionListRenderer.contents.first().itemSectionRenderer.contents.first()
            .elementRenderer.newElement.type.componentType.model.musicRadioBuilderModel.seedItems
    val mutations: List<Mutation> get() =
        frameworkUpdates.entityBatchUpdate.mutations

    data class Contents(val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer)
    data class SingleColumnBrowseResultsRenderer(val tabs: List<Tab>)
    data class Tab(val tabRenderer: TabRenderer)
    data class TabRenderer(val content: Content)
    data class Content(val sectionListRenderer: SectionListRenderer)
    data class SectionListRenderer(val contents: List<SectionListRendererContent>)
    data class SectionListRendererContent(val itemSectionRenderer: ItemSectionRenderer)
    data class ItemSectionRenderer(val contents: List<ItemSectionRendererContent>)
    data class ItemSectionRendererContent(val elementRenderer: ElementRenderer)
    data class ElementRenderer(val newElement: NewElement)
    data class NewElement(val type: Type)
    data class Type(val componentType: ComponentType)
    data class ComponentType(val model: Model)
    data class Model(val musicRadioBuilderModel: MusicRadioBuilderModel)
    data class MusicRadioBuilderModel(val seedItems: List<SeedItem>)
    data class SeedItem(val itemEntityKey: String, val musicThumbnail: MusicThumbnail, val title: String)
    data class MusicThumbnail(val image: Image)
    data class Image(val sources: List<Thumbnail>)

    data class FrameworkUpdates(val entityBatchUpdate: EntityBatchUpdate)
    data class EntityBatchUpdate(val mutations: List<Mutation>)
    data class Mutation(val entityKey: String, val payload: Payload) {
        val token: String? get() = payload.musicFormBooleanChoice?.opaqueToken
    }
    data class Payload(val musicFormBooleanChoice: MusicFormBooleanChoice?)
    data class MusicFormBooleanChoice(val opaqueToken: String)
}
