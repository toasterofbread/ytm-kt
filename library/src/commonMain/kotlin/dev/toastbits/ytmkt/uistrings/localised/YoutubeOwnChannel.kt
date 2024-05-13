package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeOwnChannelLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en_GB to "Songs on repeat",
                ja_JP to "繰り返し再生されている曲" ,
                zh_CN to "反复聆听的歌曲" ,
                es_US to "Canciones que más escuchastes",
                fr_FR to "Titres en boucle",
                tr_TR to "Tekrarlanan şarkılar",
                ru_RU to "Треки на повторе"
            )
            add(
                en_GB to "Artists on repeat",
                zh_CN to "反复聆听的歌手" ,
                ja_JP to "繰り返し再生するアーティスト" ,
                es_US to "Artistas más escuchados",
                fr_FR to "Artistes en boucle",
                tr_TR to "Tekrarlanan sanatçılar",
                ru_RU to "Исполнители на повторе"
            )
            add(
                en_GB to "Videos on repeat",
                zh_CN to "反复收看的视频" ,
                ja_JP to "繰り返し再生されている動画",
                fr_FR to "Clips en boucle",
                tr_TR to "Tekrarlanan videolar",
                ru_RU to "Видео на повторе"
            )
            add(
                en_GB to "Playlists on repeat",
                zh_CN to "反复聆听的歌单" ,
                ja_JP to "繰り返し再生するプレイリスト" ,
                es_US to "Playlist mas escuchadas",
                fr_FR to "Liste de lecture en boucle",
                tr_TR to "Tekrarlanan oynatma listeleri",
                ru_RU to "Плейлисты на повторе"
            )
            add(
                en_GB to "Playlists",
                zh_CN to "播放列表" ,
                zh_TW to "播放清單" ,
                ja_JP to "再生リスト" ,
                es_US to "Playlists",
                fr_FR to "Liste de lecture",
                tr_TR to "Oynatma listeleri",
                ru_RU to "Плейлисты"
            )
        }
    }
