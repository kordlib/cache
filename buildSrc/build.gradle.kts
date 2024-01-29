plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    // Repo providing the Kord Gradle plugin
    maven("https://europe-west3-maven.pkg.dev/mik-music/kord")
}

dependencies {
    implementation(libs.bundles.gradlePlugins)
}
