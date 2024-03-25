package dev.toastbits.ytmkt.uistrings.localised

private val amount_suffixes: Map<String, Map<String, Int>> = mapOf(
    UILanguages.en to mapOf(
        "B" to 1000000000,
        "M" to 1000000,
        "K" to 1000
    ),
    UILanguages.ja to mapOf(
        "億" to 100000000,
        "万" to 10000,
        "千" to 1000,
        "百" to 100
    ),
    UILanguages.zh to mapOf(
        "亿" to 100000000,
        "万" to 10000,
        "千" to 1000,
        "百" to 100
    ),
    UILanguages.ru to mapOf(
        "млрд." to 1000000000,
        "млн." to 1000000,
        "тыс." to 1000,
    )
)

fun getAmountSuffixes(language: String): Map<String, Int>? =
    amount_suffixes.getByLanguage(language)?.value?.value
