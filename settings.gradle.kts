pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val kotlin_version: String = extra["kotlin.version"] as String
        kotlin("multiplatform").version(kotlin_version)

        val agp_version: String = extra["agp.version"] as String
        id("com.android.library").version(agp_version)

        val dokka_version: String = extra["dokka.version"] as String
        id("org.jetbrains.dokka").version(dokka_version)
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ytm-kt"
include(":library")
include(":sample")
