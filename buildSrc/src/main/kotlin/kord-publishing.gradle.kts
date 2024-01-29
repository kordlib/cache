plugins {
    id("com.vanniktech.maven.publish.base")
    dev.kord.`gradle-tools`
}

kord {
    publicationName = "mavenCentral"
}

mavenPublishing {
    afterEvaluate {
        coordinates(Library.group, "cache-${project.name}", project.version.toString())
    }
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = Library.name
        description = Library.description
        url = "https://github.com/kordlib/cache"

        organization {
            name = "Kord"
            url = "https://github.com/kordlib"
        }

        developers {
            developer {
                name = "The Kord Team"
            }
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/kordlib/cache/issues"
        }

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
                distribution = "https://github.com/kordlib/cache/blob/LICENSE"
            }
        }

        scm {
            connection = "scm:git:ssh://github.com/kordlib/cache.git"
            developerConnection = "scm:git:ssh://git@github.com:kordlib/cache.git"
            url = "https://github.com/kordlib/cache"
        }
    }
}
