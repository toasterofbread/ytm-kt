plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose").version("1.5.12")
}

kotlin {
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":library"))

                val ktor_version: String = extra["ktor.version"] as String
                implementation("io.ktor:ktor-client-core:$ktor_version")
                // implementation("io.ktor:ktor-client-cio:$ktor_version")
                // implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                // implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.toastbits.sample.SampleKt"
    }
}
