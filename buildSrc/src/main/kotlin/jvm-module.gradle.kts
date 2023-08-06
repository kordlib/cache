import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(Jvm.target)
        }
    }
}

publishing {
    publications.register<MavenPublication>(Library.name) {
        from(components["java"])
        artifact(tasks.kotlinSourcesJar)
    }
}
