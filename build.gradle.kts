plugins {
    kotlin("multiplatform") version "1.3.50" apply false
}

repositories {
    mavenCentral()
    jcenter()
}

subprojects {
    group = "com.gitlab.kordlib.cache"
    version = "0.0.1"

    apply(from = "../config.gradle.kts")

    repositories {
        mavenCentral()
        jcenter()
    }
}
