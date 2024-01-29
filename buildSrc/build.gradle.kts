plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    mavenCentral()
    // Repo providing the Kord Gradle plugin
    maven("https://europe-west3-maven.pkg.dev/mik-music/kord")
}

dependencies {
    implementation(libs.bundles.gradlePlugins)
}
