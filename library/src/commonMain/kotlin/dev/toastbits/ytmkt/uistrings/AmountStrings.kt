package dev.toastbits.ytmkt.uistrings

import dev.toastbits.ytmkt.uistrings.localised.getAmountSuffixes

const val UNLOCALISED_STRING_TYPE = "AMOUNT_SUFFIX"

fun parseYoutubeSubscribersString(string: String, hl: String): Int? {
    val suffixes: Map<String, Int> =
        getAmountSuffixes(hl) ?: return null

    val last_digit_index: Int = string.indexOfLast { it.isDigit() }

    if (last_digit_index + 1 == string.length) {
        return string.toFloat().toInt()
    }

    val multiplier: Int? = suffixes[string.substring(last_digit_index + 1)]
    if (multiplier == null) {
        return null
    }

    return (string.substring(0, string.length - 1).toFloat() * multiplier).toInt()
}

fun amountToString(amount: Int, hl: String): String {
    val suffixes: Map<String, Int> =
        getAmountSuffixes(hl) ?: return amount.toString()

    for (suffix in suffixes) {
        if (amount >= suffix.value) {
            return "${amount / suffix.value}${suffix.key}"
        }
    }

    return amount.toString()
}

