package dev.toastbits.ytmkt.endpoint

import dev.toastbits.ytmkt.model.AuthenticatedApiEndpoint
import dev.toastbits.ytmkt.model.external.PlaylistEditor

abstract class AccountPlaylistEditorEndpoint: AuthenticatedApiEndpoint() {
    
    /**
     * Creates and returns an instance of [PlaylistEditor] using the passed playlist information.
     *
     * @param playlist_id The ID of the playlist to edit.
     * @param item_ids An ordered list of the playlist's current media item IDs.
     * @param item_set_ids An ordered list of the playlist's current item set IDs. See [YtmPlaylist.item_set_ids][dev.toastbits.ytmkt.model.external.mediaitem.YtmPlaylist.item_set_ids].
     * @return the created playlist editor.
     */
    abstract fun getEditor(
        playlist_id: String,
        item_ids: List<String>,
        item_set_ids: List<String>
    ): PlaylistEditor
}
