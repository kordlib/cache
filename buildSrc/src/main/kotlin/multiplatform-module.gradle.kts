import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    org.jetbrains.kotlin.multiplatform
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
}

kotlin {
    jvm {
        compilations.all {
            compilerOptions.options.jvmTarget.set(Jvm.target)
        }
    }

    js(IR) {
        nodejs()
        browser()
    }
    mingwX64()
}

tasks {
    getByName<KotlinJvmTest>("jvmTest") {
        useJUnitPlatform()
    }

    dokkaHtml {
        configure {
            dokkaSourceSets {
                val map = asMap

                if (map.containsKey("jsMain")) {
                    named("jsMain") {
                        displayName.set("JS")
                    }
                }

                if (map.containsKey("jvmMain")) {
                    named("jvmMain") {
                        displayName.set("JVM")
                    }
                }

                if (map.containsKey("commonMain")) {
                    named("jvmMain") {
                        displayName.set("Common")
                    }
                }
            }
        }
    }
}
