package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeFilterChipsLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en_GB to "Relax",
                ja_JP to "リラックス",
                es_US to "Relax",
                zh_CN to "放松",
                zh_TW to "放鬆",
                fr_FR to "Détente",
                tr_TR to "Rahatlatıcı",
                ru_RU to "Релакс",
                id = YoutubeUILocalisation.StringID.SONG_FEED_RELAX
            )
            add(
                en_GB to "Energise",
                en_US to "Energize",
                ja_JP to "エナジー",
                es_US to "Energía",
                zh_CN to "充电",
                zh_TW to "能量",
                fr_FR to "Énergie",
                tr_TR to "Enerjik",
                ru_RU to "Заряд энергии",
                id = YoutubeUILocalisation.StringID.SONG_FEED_ENERGISE
            )
            add(
                en_GB to "Workout",
                ja_JP to "ワークアウト",
                es_US to "Entretenimiento",
                zh_CN to "健身",
                zh_TW to "健身",
                fr_FR to "Sport",
                tr_TR to "Antrenman",
                ru_RU to "Тренировка",
                id = YoutubeUILocalisation.StringID.SONG_FEED_WORKOUT
            )
            add(
                en_GB to "Commute",
                ja_JP to "通勤",
                es_US to "Para el camino",
                zh_CN to "通勤",
                zh_TW to "通勤",
                fr_FR to "Pour la route",
                tr_TR to "Gidip Gelme",
                ru_RU to "В дороге",
                id = YoutubeUILocalisation.StringID.SONG_FEED_COMMUTE
            )
            add(
                en_GB to "Focus",
                ja_JP to "フォーカス",
                es_US to "Para concentrarse",
                zh_CN to "专注",
                zh_TW to "專注",
                fr_FR to "Concentration",
                tr_TR to "Odaklanma",
                ru_RU to "Концентрация",
                id = YoutubeUILocalisation.StringID.SONG_FEED_FOCUS
            )
            add(
                en_GB to "Podcasts",
                ja_JP to "ポッドキャスト",
                es_US to "Podcast",
                zh_CN to "播客",
                zh_TW to "Podcast",
                fr_FR to "Podcasts",
                tr_TR to "Podcast'ler",
                ru_RU to "Подкасты",
                id = YoutubeUILocalisation.StringID.SONG_FEED_PODCASTS
            )
            add(
                en_GB to "Party",
                ja_JP to "パーティー",
                zh_CN to "派对",
                zh_TW to "派對",
                fr_FR to "Fête",
                tr_TR to "Parti",
                ru_RU to "Вечеринка",
                id = YoutubeUILocalisation.StringID.SONG_FEED_PARTY
            )
            add(
                en_GB to "Romance",
                ja_JP to "ロマンス",
                zh_CN to "浪漫",
                zh_TW to "浪漫愛情",
                fr_FR to "Romance",
                tr_TR to "Romantik",
                ru_RU to "Романтика",
                id = YoutubeUILocalisation.StringID.SONG_FEED_ROMANCE
            )
            add(
                en_GB to "Sad",
                ja_JP to "悲しい",
                zh_CN to "伤心难过",
                zh_TW to "難過憂愁",
                fr_FR to "Triste",
                tr_TR to "Üzgün",
                ru_RU to "Грустная",
                id = YoutubeUILocalisation.StringID.SONG_FEED_SAD
            )
            add(
                en_GB to "Feel good",
                ja_JP to "ポジティブ",
                zh_CN to "轻松愉悦",
                zh_TW to "好心情",
                fr_FR to "Bonne humeur",
                tr_TR to "İyi Hisset",
                ru_RU to "Веселая",
                id = YoutubeUILocalisation.StringID.SONG_FEED_FEEL_GOOD
            )
            add(
                en_GB to "Sleep",
                ja_JP to "睡眠",
                zh_CN to "睡眠",
                zh_TW to "睡眠",
                fr_FR to "Sommeil",
                tr_TR to "Uyku",
                ru_RU to "Сон",
                id = YoutubeUILocalisation.StringID.SONG_FEED_SLEEP
            )
        }
    }
