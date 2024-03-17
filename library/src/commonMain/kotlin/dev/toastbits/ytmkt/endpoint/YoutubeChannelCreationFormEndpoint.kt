package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.ApiEndpoint
import dev.toastbits.ytmkt.model.external.YoutubeAccountCreationForm
import io.ktor.http.Headers

abstract class YoutubeChannelCreationFormEndpoint: ApiEndpoint() {
    abstract suspend fun getForm(
        headers: Headers,
        channel_creation_token: String,
    ): Result<YoutubeAccountCreationForm.ChannelCreationForm>
}
