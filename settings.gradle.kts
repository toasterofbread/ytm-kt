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

        id("com.vanniktech.maven.publish").version("0.28.0")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

rootProject.name = "ytm-kt"
include(":library")
include(":sample")
