pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val kotlin_version: String = extra["kotlin.version"] as String
        val agp_version: String = extra["agp.version"] as String

        kotlin("multiplatform").version(kotlin_version)
        id("com.android.library").version(agp_version)
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
