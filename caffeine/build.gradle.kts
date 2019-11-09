val caffeineVersion: String by extra

plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":api"))
    api(project(":map"))
    api("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")

    testImplementation(project(":tck"))
}