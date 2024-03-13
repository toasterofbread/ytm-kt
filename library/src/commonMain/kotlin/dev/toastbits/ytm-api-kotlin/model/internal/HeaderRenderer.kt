package dev.toastbits.ytmapi.model.internal


open class HeaderRenderer(
    val title: TextRuns?,
    val strapline: TextRuns?,
    val subscriptionButton: SubscriptionButton?,
    val playButton: MoreContentButton?,
    val description: TextRuns?,
    val thumbnail: Thumbnails?,
    val foregroundThumbnail: Thumbnails?,
    val subtitle: TextRuns?,
    val secondSubtitle: TextRuns?,
    val moreContentButton: MoreContentButton?
) {
    fun getThumbnails(): List<Thumbnail> {
        return (foregroundThumbnail ?: thumbnail)?.thumbnails ?: emptyList()
    }
}
