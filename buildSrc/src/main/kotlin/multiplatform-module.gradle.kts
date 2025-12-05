@file:OptIn(ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    org.jetbrains.kotlin.multiplatform
    org.jetbrains.dokka
    id("com.vanniktech.maven.publish.base")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("nonJvm") {
                withJs()
                withNative()
            }
        }
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add(OptIns.time)
    }

    linuxX64()
    linuxArm64()

    mingwX64()

    macosX64()
    macosArm64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosX64()
    watchosArm64()
    watchosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    abiValidation {
        enabled = true
        klib {
            enabled = true
        }
    }
}

tasks {
    getByName<KotlinJvmTest>("jvmTest") {
        useJUnitPlatform()
    }
    withType<KotlinNativeSimulatorTest>() {
        enabled = false
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
}

dokka {
    configure(project)
}
