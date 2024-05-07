# ytm-kt

A Kotlin Multiplatform library for (unofficially) using the YouTube Music API, optionally with user authentication. This library was originally a part of [SpMp](https://github.com/toasterofbread/spmp).

## Setup

ytm-kt currently supports the following Kotlin platforms:
- JVM
  - Android
  - Desktop
- Native
  - Linux x86_64
  - Linux arm64
  - Windows x86_64

#### Gradle:

1. Add the Maven Central repository to your dependency resolution configuration
```
repositories {
  mavenCentral()
}
```

2. To your dependencies, add the line corresponding to the target platform (replace `<version>` with the desired ytm-kt [version](https://github.com/toasterofbread/ytm-kt/tags))

- JVM (Android): `implementation("dev.toastbits.ytmkt:ytmkt-android:<version>")`
- JVM (desktop): `implementation("dev.toastbits.ytmkt:ytmkt-jvm:<version>")`
- Native (Linux x86_64): `implementation("dev.toastbits.ytmkt:ytmkt-linuxx64:<version>")`
- Native (Linux arm64): `implementation("dev.toastbits.ytmkt:ytmkt-linuxarm64:<version>")`
- Native (Windows x86_64): `implementation("dev.toastbits.ytmkt:ytmkt-mingwx64:<version>")`

## Usage

API endpoints are accessed through the [YtmApi](library/src/commonMain/kotlin/dev/toastbits/ytmkt/model/YtmApi.kt) interface. To use this interface, either implement it yourself or use one of the built-in implementations:

- [YoutubeiApi](library/src/commonMain/kotlin/dev/toastbits/ytmkt/impl/youtubei/YoutubeiApi.kt) - directly accesses YouTube's internal API via https://music.youtube.com/youtubei/v1/.
- [UnimplementedYtmApi](library/src/commonMain/kotlin/dev/toastbits/ytmkt/impl/unimplemented/UnimplementedYtmApi.kt) - contains dummy implementations of all APIs. Intended to be used as a base for custom API implementations.

### Example usage

```
// Initialise the Youtubei api implementation
val api: YtmApi =
    YoutubeiApi(
        data_language = "en-GB" // The language we want data (such as song names) to be in
    )

// Download the home page recommendations feed
val song_feed_result: Result<SongFeedLoadResult> = api.SongFeed.getSongFeed()

// Print feed row titles in our desired language
for (layout in song_feed_result.getOrThrow().layouts) {
  val title_text: String = layout.title.getString("en-GB") // The language we want UI strings to be in
  println("Layout $title_text has ${layout.items.size} items")
}
```

#### See the [sample application](sample/src/commonMain/kotlin/dev/toastbits/sample/Sample.kt) for a more detailed example

## Documentation

TODO
