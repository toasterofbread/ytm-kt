package dev.toastbits.ytmapi.radio

import LocalPlayerState
import SpMp.isDebugBuild
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.toasterofbread.composekit.utils.common.getContrasted
import com.toasterofbread.composekit.utils.common.launchSingle
import com.toasterofbread.composekit.utils.common.synchronizedBlock
import com.toasterofbread.composekit.utils.modifier.bounceOnClick
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItem
import dev.toastbits.ytmapi.model.external.mediaitem.MediaItemData
import dev.toastbits.ytmapi.model.external.mediaitem.Artist
import dev.toastbits.ytmapi.model.external.mediaitem.db.isMediaItemHidden
import dev.toastbits.ytmapi.model.external.mediaitem.getMediaItemFromUid
import dev.toastbits.ytmapi.model.external.mediaitem.getUid
import dev.toastbits.ytmapi.model.external.mediaitem.layout.MediaItemLayout
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.LocalPlaylist
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.LocalPlaylistData
import dev.toastbits.ytmapi.model.external.mediaitem.playlist.RemotePlaylist
import dev.toastbits.ytmapi.model.external.mediaitem.Playlist
import dev.toastbits.ytmapi.model.external.mediaitem.Song
import dev.toastbits.ytmapi.model.external.mediaitem.song.SongData
import com.toasterofbread.spmp.ui.component.ErrorInfoDisplay
import com.toasterofbread.spmp.service.playercontroller.PlayerState
import com.toasterofbread.spmp.ui.theme.appHover
import dev.toastbits.ytmapi.RadioBuilderModifier
import dev.toastbits.ytmapi.impl.youtubemusic.cast
import kotlinx.coroutines.*

open class RadioInstance(
    val api: YoutubeApi
) {
    var state: RadioState by mutableStateOf(RadioState())

    val active: Boolean get() = state.item != null
    val loading: Boolean get() = state.loading

    private val coroutine_scope = CoroutineScope(Dispatchers.IO)
    private val lock = coroutine_scope
    private var failed_load_retry_callback: (suspend (Result<List<Song>>, Boolean) -> Unit)? by mutableStateOf(null)
    
    protected open fun formatContinuationResult(result: Result<List<Song>>): Result<List<Song>> =
        result.fold(
            { songs ->
                Result.success(
                    if (state.shuffle) songs.shuffled()
                    else songs
                )
            },
            { result }
        )

    fun dismissRadioLoadError() {
        state = state.copy(
            load_error = null
        )
        failed_load_retry_callback = null
    }

    data class RadioLoadError(
        val message: String,
        val stack_trace: String,
        val can_retry: Boolean
    )

    data class RadioState(
        val item: Pair<String, Int?>? = null,
        val continuation: MediaItemLayout.Continuation? = null,
        val initial_songs_loaded: Boolean = false,
        val filters: List<List<RadioBuilderModifier>>? = null,
        val current_filter: Int? = null,
        val shuffle: Boolean = false
    ) {
        fun getCurrentFilters(): List<RadioBuilderModifier> {
            return current_filter?.let { filter ->
                if (filter == -1) listOf(RadioBuilderModifier.Internal.ARTIST)
                else filters?.get(filter)
            } ?: emptyList()
        }

        internal fun copyWithFilter(filter_index: Int?): RadioState =
            copy(
                current_filter = filter_index,
                continuation = null,
                initial_songs_loaded = false
            )

        internal fun copyWithLoadError(error: Throwable?, can_retry: Boolean): RadioState =
            copy(
                load_error = error?.let {
                    RadioLoadError(it.message.toString(), it.stackTraceToString(), can_retry)
                }
            )
    }

    fun playMediaItem(item: MediaItem, index: Int? = null, shuffle: Boolean = false): RadioState {
        synchronized(lock) {
            return setRadioState(
                RadioState(
                    item = Pair(item.getUid(), index),
                    shuffle = shuffle
                )
            )
        }
    }

    fun setFilter(filter_index: Int?) {
        synchronized(lock) {
            if (filter_index == state.current_filter) {
                return
            }
            state = state.copyWithFilter(filter_index)
            cancelJob()
        }
    }

    fun onSongRemoved(index: Int) {
        synchronizedBlock(lock) {
            val current_index = state.item?.second ?: return
            if (index == current_index) {
                cancelRadio()
            }
            else if (index < current_index) {
                state = state.copy(
                    item = state.item?.copy(second = current_index - 1)
                )
            }
        }
    }

    fun onSongMoved(from: Int, to: Int) {
        synchronized(lock) {
            if (from == to) {
                return
            }

            val current_index = state.item?.second ?: return

            if (from == current_index) {
                state = state.copy(
                    item = state.item?.copy(second = to)
                )
            }
            else if (current_index in from..to) {
                state = state.copy(
                    item = state.item?.copy(second = current_index - 1)
                )
            }
            else if (current_index in to .. from) {
                state = state.copy(
                    item = state.item?.copy(second = current_index + 1)
                )
            }
        }
    }

    fun setRadioState(new_state: RadioState): RadioState {
        synchronized(lock) {
            if (state == new_state) {
                return state
            }

            cancelJob()
            val old = state
            state = new_state
            return old
        }
    }

    fun cancelRadio(): RadioState {
        synchronized(lock) {
            val old_state = setRadioState(RadioState())
            cancelJob()
            return old_state
        }
    }

    fun cancelJob() {
        coroutine_scope.coroutineContext.cancelChildren()
        state = state.copy(loading = false)
    }

    fun isContinuationAvailable(): Boolean =
        state.continuation != null

    fun loadContinuation(
        onStart: (suspend () -> Unit)? = null,
        can_retry: Boolean = false,
        is_retry: Boolean = false,
        callback: suspend (result: Result<List<Song>>, is_retry: Boolean) -> Unit
    ) {
        val use_callback: suspend (Result<List<Song>>, Boolean) -> Unit =
            if (is_retry) {{ a, b ->
                callback(a, b)
                failed_load_retry_callback?.invoke(a, b)
                failed_load_retry_callback = null
            }}
            else callback

        synchronized(lock) {
            coroutine_scope.launchSingle {
                coroutineContext.job.invokeOnCompletion { cause ->
                    if (cause !is CancellationException) {
                        synchronized(lock) {
                            state = state.copy(loading = false)
                        }
                    }
                }
                synchronized(lock) {
                    state = state.copy(loading = true)
                    failed_load_retry_callback = null
                }

                onStart?.invoke()

                if (state.continuation == null) {
                    if (state.initial_songs_loaded) {
                        return@launchSingle
                    }

                    val initial_songs: Result<List<Song>> = getInitialSongs()
                    initial_songs.onFailure { error ->
                        synchronized(lock) {
                            state = state.copyWithLoadError(error, can_retry)
                            failed_load_retry_callback = if (can_retry) callback else null
                        }
                    }

                    val formatted = formatContinuationResult(initial_songs)
                    use_callback(formatted, is_retry)

                    state = state.copy(initial_songs_loaded = true)
                    return@launchSingle
                }

                val continuation: MediaItemLayout.Continuation? = state.continuation
                if (continuation == null) {
                    use_callback(Result.failure(NullPointerException("State continuation is null")), is_retry)
                    return@launchSingle
                }

                val result = continuation.loadContinuation(state.getCurrentFilters())
                val (items, cont) = result.fold(
                    { it },
                    { error ->
                        synchronized(lock) {
                            state = state.copyWithLoadError(error, can_retry)
                            failed_load_retry_callback = if (can_retry) callback else null
                        }
                        use_callback(result.cast(), is_retry)
                        return@launchSingle
                    }
                )

                if (cont != null) {
                    state.continuation?.update(cont)
                }
                else {
                    state = state.copy(continuation = null)
                }

                use_callback(formatContinuationResult(Result.success(items.filterIsInstance<SongData>())), is_retry)
            }
        }
    }

    private suspend fun getInitialSongs(): Result<List<Song>> {
        val item_uid: String = state.item?.first
            ?: return Result.failure(NullPointerException("State item is null"))

        when (val item: MediaItem = getMediaItemFromUid(item_uid)) {
            is Song -> {
                val result = 
                    api.SongRadio.implementedOrNull()?.getSongRadio(
                        item.id,
                        null,
                        state.getCurrentFilters()
                    )

                if (result == null) {
                    return api.SongRadio.getNotImplementedException()
                }

                return result.fold(
                    { data ->
                        state = state.copy(
                            continuation = data.continuation?.let { continuation ->
                                MediaItemLayout.Continuation(continuation, MediaItemLayout.Continuation.Type.SONG, item.id)
                            },
                            filters = state.filters ?: data.filters
                        )

                        Result.success(data.items)
                    },
                    { Result.failure(it) }
                )
            }
            is Artist -> {
                val result =
                    api.ArtistShuffle.implementedOrNull()?.getArtistShuffle(
                        artist = item,
                        continuation = null
                    )

                if (result == null) {
                    return Result.failure(api.ArtistShuffle.getNotImplementedException())
                }

                return result?.fold(
                    { data ->
                        state = state.copy(
                            continuation = data.continuation?.let { continuation ->
                                MediaItemLayout.Continuation(continuation, MediaItemLayout.Continuation.Type.SONG, item.id)
                            },
                            filters = null
                        )
                        Result.success(data.items)
                    },
                    { Result.failure(it) }
                )
            }
            is RemotePlaylist -> {
                val playlist_data: Playlist = item.loadData(context).fold(
                    { it },
                    { return Result.failure(it) }
                )

                if (playlist_data.items == null) {
                    state = state.copy(
                        continuation = playlist_data.continuation ?: MediaItemLayout.Continuation(item.id, MediaItemLayout.Continuation.Type.PLAYLIST_INITIAL)
                    )
                    return Result.success(emptyList())
                }

                state = state.copy(
                    continuation = playlist_data.continuation
                )

                return Result.success(playlist_data.items!!)
            }
            is LocalPlaylist -> {
                val data: LocalPlaylistData = item.loadData(context).fold(
                    { it },
                    { return Result.failure(it) }
                )
                return Result.success(data.items ?: emptyList())
            }
//            is MediaItemWithLayouts -> {
//                val feed_layouts = item.getFeedLayouts().fold(
//                    { it },
//                    { return Result.failure(it) }
//                )
//
//                val layout = feed_layouts.firstOrNull()
//                if (layout == null) {
//                    return Result.success(emptyList())
//                }
//
//                val view_more = layout.view_more
//                if (view_more is MediaItemLayout.MediaItemViewMore && view_more.media_item is Playlist) {
//                    state.continuation = MediaItemLayout.Continuation(view_more.media_item.id, MediaItemLayout.Continuation.Type.PLAYLIST_INITIAL, layout.items.size)
//                }
//                else {
//                    state.continuation = layout.continuation
//                }
//
//                return Result.success(layout.items.filterIsInstance<SongData>())
//            }
            else -> throw NotImplementedError(item::class.toString())
        }
    }
}
