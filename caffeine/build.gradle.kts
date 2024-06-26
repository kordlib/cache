import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `jvm-module`
    `kord-publishing`
}

kord {
    jvmTarget = JvmTarget.JVM_11 // Caffeine uses Java 11
}

dependencies {
    api(projects.api)
    api(projects.map)
    api(libs.caffeine)
    api(libs.kotlinx.coroutines)

    testImplementation(projects.tck)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}
