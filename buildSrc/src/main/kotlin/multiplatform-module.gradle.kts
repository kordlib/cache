import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import dev.kord.gradle.model.*
import dev.kord.gradle.model.targets.*
import dev.kord.gradle.model.targets.native.*

plugins {
    org.jetbrains.kotlin.multiplatform
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
    dev.kord.`kotlin-multiplatform-plugin`
}

kotlin {
    configureTargets {
        jvm {
            kotlinCompile {
                jvmTarget.set(Jvm.target)
            }
        }
        group("nonJvm") {
            fullJs()
            mingwX64()
            linuxX64()
            darwin()
        }
    }
}

kotlinMultiplatformProject {
    configure()
}

tasks {
    getByName<KotlinJvmTest>("jvmTest") {
        useJUnitPlatform()
    }
}
