 plugins {
    `multiplatform-module`
    `kotlinx-atomicfu`
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

        nonJvmMain {
            dependencies {
                implementation(libs.bundles.stately)
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
