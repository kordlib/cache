plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    // until Dokka 1.8.0 is released and we no longer need dev builds, see https://github.com/kordlib/kord/pull/755
    maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    maven("https://kord.jfrog.io/artifactory/gradle-dev/")
}

dependencies {
    implementation(libs.bundles.gradlePlugins)
    implementation(gradleApi())
    implementation(localGroovy())
}
