package dev.toastbits.ytmapi.uistrings

import dev.toastbits.ytmapi.uistrings.localised.getAmountSuffixes

const val UNLOCALISED_STRING_TYPE = "AMOUNT_SUFFIX"

fun parseYoutubeSubscribersString(string: String, hl: String): Int? {
    val suffixes = getAmountSuffixes(hl)
    if (suffixes != null) {
        if (string.last().isDigit()) {
            return string.toFloat().toInt()
        }

        val multiplier = suffixes[string.last()]
        if (multiplier == null) {
            return null
        }

        return (string.substring(0, string.length - 1).toFloat() * multiplier).toInt()
    }

    return null
}

fun amountToString(amount: Int, hl: String): String {
    val suffixes = getAmountSuffixes(hl)
    if (suffixes != null) {
        for (suffix in suffixes) {
            if (amount >= suffix.value) {
                return "${amount / suffix.value}${suffix.key}"
            }
        }

        return amount.toString()
    }

    return amount.toString()
}

