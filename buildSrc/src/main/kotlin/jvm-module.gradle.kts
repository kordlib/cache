@file:OptIn(ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    org.jetbrains.kotlin.jvm
    org.jetbrains.dokka
    id("com.vanniktech.maven.publish.base")
}

kotlin {
    abiValidation {
        enabled = true
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dokka {
    configure(project)
}

mavenPublishing {
    configure(KotlinJvm(JavadocJar.Dokka("dokkaGeneratePublicationHtml")))
}
