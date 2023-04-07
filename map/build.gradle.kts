plugins {
    `multiplatform-module`
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn(OptIns.coroutines)
        }

        commonMain {
            dependencies {
                api(projects.api)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.tck)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
            }
        }
    }
}
