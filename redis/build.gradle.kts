import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `jvm-module`
    alias(libs.plugins.koltin.serialization)
    `kord-publishing`
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

dependencies {
    api(projects.api)
    api(projects.map)
    api(libs.lettuce)
    api(libs.kotlinx.serialization.protobuf)
    api(libs.kotlinx.coroutines.reactive)

    testImplementation(projects.tck)
    testImplementation(libs.embedded.redis)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            optIn.add(OptIns.serialization)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
