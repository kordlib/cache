val coroutinesVersion: String by extra

plugins {
    kotlin("multiplatform")
}

kotlin {
    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                progressiveMode = true
            }
        }
    }

    sourceSets.commonMain.get().dependencies {
        implementation(kotlin("stdlib-common"))
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
    }

    sourceSets.commonTest.get().dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvm {
        val main by compilations.getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }

            kotlinOptions {
                jvmTarget = "1.8"
            }

        }

    }

    js {
        val main by compilations.getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += listOf(
                        "-XXLanguage:+InlineClasses",
                        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                        "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
                        "-Xuse-experimental=kotlin.experimental.ExperimentalTypeInference"
                )
            }
        }
    }

}
