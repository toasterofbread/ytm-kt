package dev.toastbits.ytmkt.uistrings.localised

import dev.toastbits.ytmkt.uistrings.YoutubeUILocalisation

fun getYoutubeSongFeedLocalisations(languages: UILanguages): YoutubeUILocalisation.LocalisationSet =
    with(languages) {
        YoutubeUILocalisation.LocalisationSet().apply {
            add(
                en_GB to "Listen again",
                ja_JP to "もう一度聴く",
                zh_CN to "再次收听",
                es_US to "Volver a escuchar",
                fr_FR to "Réécouter",
                tr_TR to "Yeniden dinleyin",
                ru_RU to "Послушать ещё раз"
            )
            add(
                en_GB to "Quick picks",
                ja_JP to "おすすめ",
                zh_CN to "快速收听",
                zh_TW to "歌曲快選",
                es_US to "Selección rápida",
                fr_FR to "Sélection rapide",
                tr_TR to "Hızlı seçimler",
                ru_RU to "Быстрая подборка"
            )
            add(
                en_GB to "START RADIO BASED ON A SONG",
                zh_CN to "根据一首歌开启电台",
                ja_JP to "曲を選んでラジオを再生",
                es_US to "Iniciar la radio basada en una canción",
                fr_FR to "Démarrer une radio basée sur un titre",
                tr_TR to "Bir şarkıya göre radyoyu başlatın",
                ru_RU to "ЗАПУСТИТЬ РАДИОСТАНЦИЮ НА ОСНОВЕ ТРЕКА"
            )
            add(
                en_GB to "Covers and remixes",
                ja_JP to "カバーとリミックス" ,
                zh_CN to "翻唱和混音",
                zh_TW to "翻唱和重混",
                es_US to "Covers y remixes",
                fr_FR to "Reprises et remix",
                tr_TR to "Kapaklar ve resimler",
                ru_RU to "Каверы и ремиксы"
            )
            add(
                en_GB to "Recommended albums",
                ja_JP to "おすすめのアルバム" ,
                zh_CN to "推荐的专辑",
                zh_TW to "推薦專輯",
                es_US to "Álbumes recomendados",
                fr_FR to "Albums recommandés",
                tr_TR to "Önerilen albümler",
                ru_RU to "Рекомендованные альбомы"
            )
            add(
                en_GB to "Forgotten favourites",
                ja_JP to "最近聞いていないお気に入り" ,
                zh_CN to "昔日最爱",
                zh_TW to "重溫舊愛",
                es_US to "Favoritos olvidados",
                fr_FR to "Favoris à redécouvrir",
                tr_TR to "Unutulan favoriler",
                ru_RU to "Забытые избранные"
            )
            add(
                en_GB to "From your library",
                zh_CN to "来自你的库" ,
                ja_JP to "ライブラリから",
                es_US to "De tu biblioteca",
                fr_FR to "De votre bibliothèque",
                tr_TR to "Kütüphanenizden",
                ru_RU to "Из вашей библиотеки"
            )
            add(
                en_GB to "From the community",
                ja_JP to "コミュニティから",
                zh_CN to "来自社区",
                es_US to "De la comunidad",
                fr_FR to "De la communauté",
                tr_TR to "Topluluktan",
                ru_RU to "От сообщества"
            )
            add(
                en_GB to "Recommended music videos",
                ja_JP to "おすすめのミュージック ビデオ",
                zh_CN to "推荐的 MV" ,
                zh_TW to "推薦的音樂影片" ,
                es_US to "Videos musicales recomendados",
                fr_FR to "Clips musicaux recommandés",
                tr_TR to "Önerilen müzik videoları",
                ru_RU to "Рекомендованные видеоклипы"
            )
            add(
                en_GB to "Live performances",
                ja_JP to "ライブ" ,
                zh_CN to "现场表演" ,
                es_US to "Presentaciones en vivo",
                fr_FR to "Concerts",
                tr_TR to "Canlı performanslar",
                ru_RU to "Лайв-выступления"
            )
            add(
                en_GB to "Recommended radios",
                ja_JP to "おすすめのラジオ",
                zh_CN to "推荐的电台",
                es_US to "Estaciones de radio recomendadas",
                fr_FR to "Radios recommandées",
                tr_TR to "Önerilen radyolar",
                ru_RU to "Рекомендованные радиостанции",
                id = YoutubeUILocalisation.StringID.FEED_ROW_RADIOS
            )
            add(
                en_GB to "FOR YOU",
                zh_CN to "为你精选",
                ja_JP to "あなたへのおすすめ",
                es_US to "Para ti",
                fr_FR to "Pour vous",
                tr_TR to "Senin için",
                ru_RU to "ДЛЯ ВАС"
            )
            add(
                en_GB to "Trending songs",
                ja_JP to "急上昇曲",
                zh_CN to "热门歌曲",
                zh_TW to "熱門歌曲",
                es_US to "Canciónes del momento",
                fr_FR to "Titres tendance",
                tr_TR to "Trend şarkılar",
                ru_RU to "Тренды"
            )
            add(
                en_GB to "Rock Artists",
                ja_JP to "ロック アーティスト",
                zh_CN to "摇滚歌手",
                es_US to "Artistas de Rock",
                fr_FR to "Artistes de Rock",
                tr_TR to "Rock sanatçıları",
                ru_RU to "Рок исполнители"
            )
            add(
                en_GB to "Hits by decade",
                ja_JP to "Hits by decade",
                zh_CN to "年代金曲",
                ja_JP to "年代別のヒット",
                fr_FR to "Succès par décennie",
                tr_TR to "On yıla göre hitler",
                ru_RU to "Хиты десятилетия"
            )
            add(
                en_GB to "JUST UPDATED",
                ja_JP to "JUST UPDATED",
                zh_CN to "最近更新",
                ja_JP to "最近の更新",
                fr_FR to "Mises à jour récentes",
                tr_TR to "Şimdi güncellenenler",
                ru_RU to "НОВИНКИ"
            )
            add(
                en_GB to "Today's hits",
                ja_JP to "Today's hits",
                zh_CN to "今日热门",
                ja_JP to "今日のヒット",
                fr_FR to "Les tubes du jour",
                tr_TR to "Bugünün hitleri",
                ru_RU to "Хиты дня"
            )
            add(
                en_GB to "Long listening",
                ja_JP to "長編ミュージック ビデオ",
                zh_CN to "长时间聆听",
                es_US to "Reproducción prolongada",
                fr_FR to "Écoute prolongée",
                tr_TR to "Uzun dinleme",
                ru_RU to "Долгое прослушивание"
            )
            add(
                en_GB to "Celebrating Africa Month",
                ja_JP to "Celebrating Africa Month",
                zh_CN to "庆祝非洲月",
                ja_JP to "アフリカ月を祝う",
                fr_FR to "Célébrons le Mois de l'Afrique",
                tr_TR to "Afrika ayı kutlaması",
            )
            add(
                en_GB to "Feeling good",
                ja_JP to "Feeling good",
                zh_CN to "欢快",
                zh_TW to "歡樂時光",
                ja_JP to "いい気分",
                fr_FR to "Bonne humeur",
                tr_TR to "İyi hisset",
                ru_RU to "Веселая"
            )
            add(
                en_GB to "Fresh new music",
                ja_JP to "Fresh new music",
                zh_CN to "全新音乐",
                ja_JP to "新鮮な曲",
                fr_FR to "Nouveautés",
                tr_TR to "Taze yeni müzik",
                ru_RU to "Свежая новая музыка"
            )
            add(
                en_GB to "#TBT",
                zh_CN to "#TBT" ,
                ja_JP to "#TBT"
            )
            add(
                en_GB to "From your library",
                ja_JP to "ライブラリから" ,
                zh_CN to "来自你的库" ,
                es_US to "De tu biblioteca",
                fr_FR to "De votre bibliothèque",
                tr_TR to "Kütüphanenizden",
                ru_RU to "Из вашей библиотеки"
            )
            add(
                es_US to "Nuevos Lanzamientos"
            )
            add(
                es_US to "Mixes para ti"
            )
            add(
                es_US to "Playlist recomendadas"
            )
            add(
                en_GB to "Recaps",
                ja_JP to "ハイライト",
                tr_TR to "Özetler",
                ru_RU to "Рекапы"
            )
            add(
                ja_JP to "卒業 〜旅立ちの季節〜"
            )
            add(
                en_GB to "Anime & Soundtracks",
                ja_JP to "アニメ＆サントラ",
                tr_TR to "Anime & Müzikler",
                ru_RU to "Аниме и Саундтреки"
            )
            add(
                en_GB to "Vocaloid & Utaite",
                ja_JP to "ボカロ&歌い手",
                tr_TR to "Vocaloid & Utaite",
                ru_RU to "Вокалоиды и Утайте"

            )
            add(
                en_GB to "Trending community playlists",
                ja_JP to "急上昇のコミュニティ再生リスト",
                ru_RU to "Популярные плейлисты от сообщества"
            )
            add(
                en_GB to "Trending in Shorts",
                zh_TW to "在 Shorts 中聆聽過的歌曲"
                ja_JP to "急上昇のショート動画",
                tr_TR to "Kısalarda trend",
                ru_RU to "Популярно в Shorts"

            )
            add(
                en_GB to "Focus",
                ja_JP to "フォーカス",
                tr_TR to "Odaklanma",
                ru_RU to "Концентрация"
            )
            add(
                ja_JP to "お気に入り"
            )
            add(
                ja_JP to "懐かしのヒット"
            )
            add(
                en_GB to "Vocaloid",
                ja_JP to "ボカロ",
                ru_RU to "Вокалоид"
            )
            add(
                ja_JP to "話題のヒット"
            )
            add(
                ja_JP to "年代別ヒット"
            )
            add(
                ja_JP to "オールタイム・ヒット"
            )
            add(
                en_GB to "Soothing tunes"
                zh_TW to "流行音樂播放清單"
            )
            add(
                en_GB to "Today's fresh & popular"
                zh_TW to "最新上架及熱門歌曲"
            )
            add(
                en_GB to "Pop playlists"
                zh_TW to "流行音樂播放清單"
            )
            add(
                en_GB to "New releases"
                zh_TW to "最新發行"
            )
            add(
                en_GB to "In the zone"
                zh_TW to "專注力音樂"
            )
            add(
                en_GB to "Time to sweat"
                zh_TW to "運動音樂"
            )
            add(
                en_GB to "Party music"
                zh_TW to "派對音樂"
            )
            add(
                en_GB to "Keep listening"
                zh_TW to "繼續收聽"
            )
            add(
                en_GB to "Rock moods"
                zh_TW to "搖滾情境"
            )
            add(
                en_GB to "Mixed for you"
                zh_TW to "為你推薦的合輯"
            )
            add(
                en_GB to "Charts"
                zh_TW to "排行榜"
            )
            add(
                en_GB to "Your shows"
                zh_TW to "你的 Podcast 節目"
            )
            
        }
    }
