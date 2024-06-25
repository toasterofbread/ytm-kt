import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    val native_targets = listOf(
        linuxX64(),
        linuxArm64(),
        mingwX64()
    )

    for (target in native_targets) {
        target.binaries {
            executable {
                entryPoint = "dev.toastbits.sample.main"

                if (target.name == "mingwX64") {
                    linkerOpts("-l:libstdc++.a")
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":library"))

                val ktor_version: String = extra["ktor.version"] as String
                implementation("io.ktor:ktor-client-core:$ktor_version")
            }
        }
    }
}

tasks.withType<KotlinNativeLink> {
    val link_task: KotlinNativeLink = this

    finalizedBy( tasks.create("${name}Finalise") {
        doFirst {
            val patch_command: String = System.getenv("KOTLIN_BINARY_PATCH_COMMAND")?.takeIf { it.isNotBlank() } ?: return@doFirst

            for (dir in link_task.outputs.files) {
                for (file in dir.listFiles().orEmpty()) {
                    if (!file.isFile) {
                        continue
                    }

                    Runtime.getRuntime().exec(arrayOf(patch_command, file.absolutePath)).waitFor()
                }
            }
        }
    } )
}
