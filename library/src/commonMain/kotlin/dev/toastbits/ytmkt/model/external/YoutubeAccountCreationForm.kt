package dev.toastbits.ytmkt.model.external

import dev.toastbits.ytmkt.model.internal.TextRuns
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeAccountCreationForm(
    val channelCreation: ChannelCreation
) {
    data class InputField(val initial_value: String?, val key: Key) {
        enum class Key {
            GIVEN_NAME,
            FAMILY_NAME;

            fun getParameterName(): String =
                when (this) {
                    GIVEN_NAME -> "givenName"
                    FAMILY_NAME -> "familyName"
                }
        }
    }

    @Serializable
    data class ChannelCreation(val channelCreationForm: ChannelCreationForm)
    @Serializable
    data class ChannelCreationForm(val contents: Contents, val buttons: List<Button>) {
        fun getChannelCreationToken(): String? {
            for (button in buttons) {
                val token = button.buttonRenderer.serviceEndpoint?.channelCreationServiceEndpoint?.channelCreationToken
                if (token != null) {
                    return token
                }
            }
            return null
        }
    }
    @Serializable
    data class Contents(val createCoreIdentityChannelContentRenderer: CreateCoreIdentityChannelContentRenderer)
    @Serializable
    data class CreateCoreIdentityChannelContentRenderer(
        val collectGivenName: Boolean,
        val givenNameValue: String?,
        val collectFamilyName: Boolean,
        val familyNameValue: String?,
        val profilePhoto: ProfilePhoto
    ) {
        @Serializable
        data class ProfilePhoto(val thumbnails: List<Thumbnail>)
        @Serializable
        data class Thumbnail(val url: String)

        fun getInputFields(): List<InputField> {
            val ret: MutableList<InputField> = mutableListOf()
            if (collectGivenName) {
                ret.add(InputField(givenNameValue, InputField.Key.GIVEN_NAME))
            }
            if (collectFamilyName) {
                ret.add(InputField(familyNameValue, InputField.Key.FAMILY_NAME))
            }
            return ret
        }
    }
    @Serializable
    data class Button(val buttonRenderer: FormButtonRenderer)
    @Serializable
    data class FormButtonRenderer(val serviceEndpoint: FormServiceEndpoint?)
    @Serializable
    data class FormServiceEndpoint(val channelCreationServiceEndpoint: ChannelCreationServiceEndpoint)
    @Serializable
    data class ChannelCreationServiceEndpoint(val channelCreationToken: String)
}