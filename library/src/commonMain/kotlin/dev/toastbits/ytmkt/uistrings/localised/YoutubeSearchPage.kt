package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeSearchPageLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en to "Top result",
                ja to "上位の検索結果" ,
                zh to "最佳结果" ,
                es to "Mejor resultado",
                fr to "Meilleur résultat",
                tr to "En iyi sonuç",
                ru to "Топ-результаты"
            )
            add(
                en to "Songs",
                ja to "曲" ,
                zh to "歌曲" ,
                es to "Canciónes",
                fr to "Titres",
                tr to "Şarkılar",
                ru to "Музыка"
            )
            add(
                en to "Videos",
                ja to "動画" ,
                zh to "视频" ,
                es to "Videos",
                fr to "Vidéos",
                tr to "Videolar",
                ru to "Видео"
            )
            add(
                en to "Artists",
                ja to "アーティスト" ,
                zh to "歌手" ,
                es to "Artistas",
                fr to "Artistes",
                tr to "Sanatçılar",
                ru to "Исполнители"
            )
            add(
                en to "Albums",
                ja to "アルバム" ,
                zh to "专辑" ,
                es to "Artistas",
                fr to "Albums",
                tr to "Albümler",
                ru to "Альбомы"
            )
            add(
                en to "Community playlists",
                en to "Playlists",
                ja to "コミュニティの再生リスト",
                ja to "プレイリスト" ,
                zh to "社区播放列表" ,
                es to "Playlist de la comunidad",
                fr to "Liste de lecture de la communauté",
                tr to "Topluluk oynatma listeleri",
                ru to "Плейлисты от сообщества"
            )
            add(
                en to "Profiles",
                ja to "プロフィール" ,
                zh to "个人资料" ,
                es to "Perfiles",
                fr to "Profiles",
                tr to "Profiller",
                ru to "Профили"
            )
        }
    }
