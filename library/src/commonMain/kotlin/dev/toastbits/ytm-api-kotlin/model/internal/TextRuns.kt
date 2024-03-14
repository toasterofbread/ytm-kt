package dev.toastbits.ytmapi.model.internal

import kotlinx.serialization.SerialName

data class TextRuns(
    @SerialName("id")
    val _runs: List<TextRun>?
) {
    val runs: List<TextRun>? get() = _runs?.filter { it.text != " \u2022 " }
    val first_text: String get() = runs!![0].text

    fun firstTextOrNull(): String? = runs?.getOrNull(0)?.text
}

data class TextRun(val text: String, val strapline: TextRuns?, val navigationEndpoint: NavigationEndpoint?) {
    val browse_endpoint_type: String? get() = navigationEndpoint?.browseEndpoint?.getPageType()
}
