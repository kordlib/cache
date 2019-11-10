plugins {
    kotlin("multiplatform") version "1.3.50" apply true
}

val config = file("config.gradle.kts")

allprojects {
    group = "com.gitlab.kordlib.cache"
    version = "0.0.1"

    apply(from = config)

    repositories {
        mavenCentral()
        jcenter()
    }
}
