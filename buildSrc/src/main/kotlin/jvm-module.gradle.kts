import gradle.kotlin.dsl.accessors._2b10aabb5184152b7fe73b0cdbb3f895.kotlinMultiplatformProject
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    org.jetbrains.kotlin.jvm
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
    `maven-publish`
    dev.kord.`kotlin-multiplatform-plugin`
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

kotlinMultiplatformProject {
    configure()
}

publishing {
    publications.register<MavenPublication>(Library.name) {
        from(components["java"])
        artifact(tasks.kotlinSourcesJar)
    }
}
