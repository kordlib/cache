import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    org.jetbrains.dokka
    org.jetbrains.kotlin.multiplatform
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.koltin.allopen)
}

allprojects {
    repositories {
        mavenCentral()
        // until Dokka 1.8.0 is released and we no longer need dev builds, see https://github.com/kordlib/kord/pull/755
        maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    }

    group = Library.group
    version = Library.version
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
