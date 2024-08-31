plugins {
    kotlin("multiplatform").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.dokka").apply(false)
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.2"
}

apiValidation {
    ignoredProjects += listOf(
        "sample",
    )

    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
}
