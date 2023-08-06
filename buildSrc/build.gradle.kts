plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.bundles.gradlePlugins)
    implementation(gradleApi())
    implementation(localGroovy())
}
