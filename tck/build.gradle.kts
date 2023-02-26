@Suppress("DSL_SCOPE_VIOLATION") // false positive for `libs` in IntelliJ
plugins {
    `multiplatform-module`
    alias(libs.plugins.koltin.serialization)
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn(OptIns.coroutines)
        }

        commonMain {
            dependencies {
                dependencies {
                    api(projects.api)
                    api(libs.bundles.test.common)
                    api(libs.kotlinx.coroutines.test)
                    api(libs.kotlinx.serialization.json)
                }
            }
        }

        jvmMain {
            dependencies {
                api(libs.kotlin.test.junit5)
            }
        }

        jsMain {
            dependencies {
                api(libs.kotlin.test.js)
            }
        }
    }
}

