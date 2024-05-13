package dev.toastbits.ytmkt.uistrings.localised

object UILanguages {
    val en_GB: String = "en-GB"
    val en_US: String = "en-US"
    val ja_JP: String = "ja-JP"
    val pl_PL: String = "pl-PL"
    val es_US: String = "es-US"
    val zh_CN: String = "zh-CN"
    val zh_TW: String = "zh-TW"
    val fr_FR: String = "fr-FR"
    val tr_TR: String = "tr-TR"
    val ru_RU: String = "ru-RU"
}

fun <T> Map<String, T>.getByLanguage(language: String): IndexedValue<Map.Entry<String, T>>? {
    val exact: IndexedValue<Map.Entry<String, T>>? = entries.withIndex().firstOrNull { it.value.key == language }
    if (exact != null) {
        return exact
    }

    val primary: String = language.primary_language
    for (entry in entries.withIndex()) {
        if (entry.value.key.primary_language == primary) {
            return entry
        }
    }

    return null
}

val String.primary_language: String get() = this.split('-', limit = 2).first()

fun String.matchesLanguage(other: String): Boolean {
    return this == other || this.primary_language == other.primary_language
}
