import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    `multiplatform-module`
    org.jetbrains.kotlinx.atomicfu
    `kord-publishing`
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn(OptIns.coroutines)
        }

        commonMain {
            dependencies {
                api(libs.kotlin.logging)
                api(libs.kotlinx.coroutines)

                compileOnly(libs.kotlinx.atomicfu)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.test.common)
            }
        }

        jvmMain {
            dependencies {
                api(libs.slf4j.api)
            }
        }

        nonJvmMain {
            dependencies {
                api(libs.bundles.stately)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
            }
        }
        jsTest {
            dependencies {
                implementation(libs.kotlin.test.js)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
    }
}
