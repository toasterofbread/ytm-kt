package dev.toastbits.ytmkt.model.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TextRuns(
    @SerialName("runs")
    val _runs: List<TextRun>?
) {
    val runs: List<TextRun>? get() = _runs?.filter { it.text != " \u2022 " }
    val first_text: String get() = runs!![0].text

    fun firstTextOrNull(): String? = runs?.getOrNull(0)?.text
}

@Serializable
data class TextRun(val text: String, val strapline: TextRuns?, val navigationEndpoint: NavigationEndpoint?) {
    val browse_endpoint_type: String? get() = navigationEndpoint?.browseEndpoint?.getPageType()
}
