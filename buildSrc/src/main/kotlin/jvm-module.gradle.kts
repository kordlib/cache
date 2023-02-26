plugins {
    org.jetbrains.kotlin.jvm
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
    `maven-publish`
}

tasks {
    test {
        useJUnitPlatform()
    }

    dokkaHtml {
        configure()
    }
}

publishing {
    publications.register<MavenPublication>(Library.name) {
        from(components["java"])
        artifact(tasks.kotlinSourcesJar)
    }
}
