import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") // false positive for `libs` in IntelliJ
plugins {
    `jvm-module`
    alias(libs.plugins.koltin.serialization)
    `maven-publish`
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
