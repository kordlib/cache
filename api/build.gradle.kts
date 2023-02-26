plugins {
    `multiplatform-module`
    `kotlinx-atomicfu`
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
                api(libs.bundles.stately)

                compileOnly(libs.kotlinx.atomicfu)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.test.common)
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
}
