import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        pom {
            name.set("ytm-kt")
            description.set("A Kotlin library for scraping data from YouTube Music")
            url.set("https://github.com/toasterofbread/ytm-kt")

            licenses {
                license {
                    name.set("GPL-3.0")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                }
            }
            developers {
                developer {
                    id.set("toasterofbread")
                    name.set("Talo Halton")
                    email.set("talohalton@gmail.com")
                    url.set("https://github.com/toasterofbread")
                }
            }
            scm {
                connection.set("https://github.com/toasterofbread/ytm-kt.git")
                url.set("https://github.com/toasterofbread/ytm-kt")
            }
            issueManagement {
                system.set("Github")
                url.set("https://github.com/toasterofbread/ytm-kt/issues")
            }
        }
    }
}

// signing {
//     if (project.hasProperty("signing.gnupg.keyName")) {
//         useGpgCmd()
//         sign(publishing.publications)
//     }
// }
