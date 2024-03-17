package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.PlaylistEditor

abstract class AccountPlaylistEditorEndpoint: AuthenticatedApiEndpoint() {
    abstract fun getEditor(
        playlist_id: String,
        item_ids: List<String>,
        item_set_ids: List<String>
    ): PlaylistEditor
}
