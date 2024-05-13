package dev.toastbits.ytmkt.uistrings.localised

data class HMSData(val hours: String, val minutes: String, val seconds: String, val splitter: String = "")

fun getHoursMinutesSecondsSuffixes(hl: String?): HMSData? =
    when (hl?.split('-', limit = 2)?.firstOrNull()) {
        "en_GB", null -> HMSData("hours", "minutes", "seconds", " ")
        "ja_JP" -> HMSData("時間", "分", "秒")
        "zh_CN" -> HMSData("时间", "分", "秒")
        "zh_TW" -> HMSData("時間", "分", "秒")
        "fr_FR" -> HMSData("heures", "minutes", "secondes", " ")
        "tr_TR" -> HMSData("saat", "dakika", "saniye", " ")
        "ru_RU" -> HMSData("часы", "минуты", "секунды", " ")
        else -> null
    }
