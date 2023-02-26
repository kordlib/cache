plugins {
    `jvm-module`
    `kord-publishing`
}

dependencies {
    api(projects.api)
    api(projects.map)
    api(libs.caffeine)
    api(libs.kotlinx.coroutines)

    testImplementation(projects.tck)
}
