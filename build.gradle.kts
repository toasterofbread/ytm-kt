plugins {
    kotlin("multiplatform").apply(false)
    id("com.android.library").apply(false)
    id("maven-publish")
}

group = "dev.toastbits.ytmkt"
version = "0.1.0"

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name.set("ytm-kt")
                description.set("A Kotlin library for scraping data from YouTube Music ")
                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                url.set("https://github.com/toasterofbread/ytm-kt")
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/toasterofbread/ytm-kt/issues")
                }
                scm {
                    connection.set("https://github.com/toasterofbread/ytm-kt.git")
                    url.set("https://github.com/toasterofbread/ytm-kt")
                }
                developers {
                    developer {
                        name.set("Talo Halton")
                        email.set("talohalton@gmail.com")
                        url.set("https://toastbits.dev")
                    }
                }
            }
        }
    }
}
