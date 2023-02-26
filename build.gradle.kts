plugins {
    org.jetbrains.dokka
}

allprojects {
    repositories {
        mavenCentral()
        // until Dokka 1.8.0 is released and we no longer need dev builds, see https://github.com/kordlib/kord/pull/755
        maven("https://maven.pkg.jetbrains.space/kotlin/p/dokka/dev")
    }

    group = Library.group
    version = Library.version
}
