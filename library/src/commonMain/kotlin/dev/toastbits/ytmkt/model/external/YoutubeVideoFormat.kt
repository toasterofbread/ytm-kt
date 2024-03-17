package dev.toastbits.ytmkt.model.external

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeVideoFormat(
    val itag: Int?,
    val mimeType: String,
    val bitrate: Int,
    val url: String?,
    val loudness_db: Float? = null
) {
    fun isAudioOnly(): Boolean =
        mimeType.startsWith("audio")

    override fun toString(): String {
        return "YoutubeVideoFormat(itag=$itag, mimeType=$mimeType, bitrate=$bitrate, loudness_db=$loudness_db, url=$url)"
    }
}

@Serializable
internal data class YoutubeFormatsResponse(
    val playabilityStatus: PlayabilityStatus,
    val streamingData: StreamingData?,
    val playerConfig: PlayerConfig?
) {
    @Serializable
    data class StreamingData(val formats: List<YoutubeVideoFormat>, val adaptiveFormats: List<YoutubeVideoFormat>)
    @Serializable
    data class PlayabilityStatus(val status: String)

    @Serializable
    data class PlayerConfig(val audioConfig: AudioConfig?)
    @Serializable
    data class AudioConfig(val loudnessDb: Float?)
}
