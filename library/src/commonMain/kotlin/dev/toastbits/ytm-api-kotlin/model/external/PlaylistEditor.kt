package dev.toastbits.ytmapi.model.external

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

interface PlaylistEditor {
    sealed class Action(val changes_items: Boolean = false) {
        data class SetTitle(val title: String): Action()
        data class SetImage(val image_url: String?): Action()
        data class SetImageWidth(val image_width: Float?): Action()

        data class Add(val song_id: String, val index: Int?): Action(true)
        data class Remove(val index: Int): Action(true)
        data class Move(val from: Int, val to: Int): Action(true) {
            init {
                require(from != to) { from.toString() }
                require(from >= 0) { from.toString() }
                require(to >= 0) { to.toString() }
            }
        }
    }

    fun canPerformDeletion(): Boolean = false

    suspend fun performAndCommitActions(actions: List<Action>): Result<Unit>
    suspend fun performDeletion(): Result<Unit>
}
