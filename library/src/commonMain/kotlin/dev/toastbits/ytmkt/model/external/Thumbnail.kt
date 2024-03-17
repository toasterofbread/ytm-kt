package dev.toastbits.ytmkt.model.external

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnail(val url: String, val width: Int, val height: Int)
