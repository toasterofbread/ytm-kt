package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeArtistPageLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en_GB to "Songs",
                ja_JP to "曲",
                es_US to "Canciónes",
                zh_CN to "歌曲",
                fr_FR to "Titres",
                tr_TR to "Müzikler",
                ru_RU to "Треки",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_SONGS
            )
            add(
                en_GB to "Albums",
                ja_JP to "アルバム",
                es_US to "Álbumes",
                zh_CN to "专辑",
                fr_FR to "Albums",
                tr_TR to "Albümler",
                ru_RU to "Альбомы",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_OTHER
            )
            add(
                en_GB to "Videos",
                ja_JP to "動画",
                es_US to "Videos",
                zh_CN to "视频",
                fr_FR to "Vidéos",
                tr_TR to "Videolar",
                ru_RU to "Видео",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_VIDEOS
            )
            add(
                en_GB to "Singles",
                ja_JP to "シングル",
                es_US to "Sencillos",
                zh_CN to "单曲",
                fr_FR to "Singles",
                tr_TR to "Tekler",
                ru_RU to "Синглы",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_SINGLES
            )
            add(
                en_GB to "Playlists",
                ja_JP to "プレイリスト",
                es_US to "Playlists",
                zh_CN to "播放列表",
                fr_FR to "Liste de lecture",
                tr_TR to "Oynatma listeleri",
                ru_RU to "Плейлисты",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_OTHER
            )
            add(
                en_GB to "From your library",
                ja_JP to "ライブラリから",
                es_US to "De tu biblioteca",
                zh_CN to "来自你的库",
                fr_FR to "De votre bibliothèque",
                tr_TR to "Kütüphanenizden",
                ru_RU to "Из вашей библиотеки",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_OTHER
            )
            add(
                en_GB to "Fans might also like",
                en_GB to "Similar artists",
                ja_JP to "おすすめのアーティスト",
                ja_JP to "似てるかも",
                es_US to "A los fans también podrían gustarles",
                zh_CN to "粉丝可能还会喜欢",
                fr_FR to "Les fans pourraient également aimer",
                fr_FR to "Artistes similaires",
                tr_TR to "Hayranlar şunları da beğenebilir",
                tr_TR to "Benzer sanatçılar",
                ru_RU to "Фанатам также нравится",
                id = YoutubeUILocalisation.StringID.ARTIST_ROW_ARTISTS
            )
            add(
                en_GB to "Featured on",
                ja_JP to "収録プレイリスト" ,
                zh_CN to "精选",
                es_US to "Aparece en",
                fr_FR to "Mis en avant sur",
                tr_TR to "Öne çıkanlar",
                ru_RU to "Представлено на"
            )
        }
    }
