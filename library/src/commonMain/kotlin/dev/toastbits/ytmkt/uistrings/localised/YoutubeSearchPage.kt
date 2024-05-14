package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeSearchPageLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en_GB to "Top result",
                ja_JP to "上位の検索結果" ,
                zh_CN to "最佳结果" ,
                zh_TW to "熱門搜尋結果" ,
                es_US to "Mejor resultado",
                fr_FR to "Meilleur résultat",
                tr_TR to "En iyi sonuç",
                ru_RU to "Топ-результаты"
            )
            add(
                en_GB to "Songs",
                ja_JP to "曲" ,
                zh_CN to "歌曲" ,
                zh_TW to "歌曲" ,
                es_US to "Canciónes",
                fr_FR to "Titres",
                tr_TR to "Şarkılar",
                ru_RU to "Музыка"
            )
            add(
                en_GB to "Videos",
                ja_JP to "動画" ,
                zh_CN to "视频" ,
                zh_TW to "影片" ,
                es_US to "Videos",
                fr_FR to "Vidéos",
                tr_TR to "Videolar",
                ru_RU to "Видео"
            )
            add(
                en_GB to "Artists",
                ja_JP to "アーティスト" ,
                zh_CN to "歌手" ,
                zh_TW to "藝人" ,
                es_US to "Artistas",
                fr_FR to "Artistes",
                tr_TR to "Sanatçılar",
                ru_RU to "Исполнители"
            )
            add(
                en_GB to "Albums",
                ja_JP to "アルバム" ,
                zh_CN to "专辑" ,
                zh_TW to "專輯" ,
                es_US to "Artistas",
                fr_FR to "Albums",
                tr_TR to "Albümler",
                ru_RU to "Альбомы"
            )
            add(
                en_GB to "Community playlists",
                en_GB to "Playlists",
                ja_JP to "コミュニティの再生リスト",
                ja_JP to "プレイリスト" ,
                zh_CN to "社区播放列表" ,
                zh_TW to "社群播放清單" ,
                es_US to "Playlist de la comunidad",
                fr_FR to "Liste de lecture de la communauté",
                tr_TR to "Topluluk oynatma listeleri",
                ru_RU to "Плейлисты от сообщества"
            )
            add(
                en_GB to "Profiles",
                ja_JP to "プロフィール" ,
                zh_CN to "个人资料" ,
                zh_TW to "個人資料" ,
                es_US to "Perfiles",
                fr_FR to "Profiles",
                tr_TR to "Profiller",
                ru_RU to "Профили"
            )
            add(
                en_GB to "Episodes",
                zh_TW to "單集節目" 
            )
        }
    }
