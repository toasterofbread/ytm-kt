package dev.toastbits.ytmapi.endpoint

import dev.toastbits.ytmapi.model.external.PlaylistEditor
import dev.toastbits.ytmapi.YoutubeApi

abstract class AccountPlaylistEditorEndpoint: YoutubeApi.UserAuthState.UserAuthEndpoint() {
    abstract fun getEditor(playlist_id: String, item_set_ids: List<String>): PlaylistEditor
}
