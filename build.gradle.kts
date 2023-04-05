import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    org.jetbrains.dokka
    org.jetbrains.kotlin.multiplatform
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.koltin.allopen)
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = Library.group
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
    }

    sourceSets {
        all {
            languageSettings.optIn(OptIns.coroutines)
        }
        commonMain {
            dependencies {
                implementation(libs.kotlinx.benchmark)
                implementation(projects.map)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

allOpen {
    // Required by JMH
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("jvm") {
            (this as JvmBenchmarkTarget).jmhVersion = libs.versions.jmh.get()
        }
        register("js")
    }
}

tasks {
    afterEvaluate {
        "jsBenchmark"(NodeJsExec::class) {
            nodeArgs.addAll(listOf("--max-old-space-size=8000"))
        }
    }
}
