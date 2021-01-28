dependencies {
    api(api)
    api(map)
    api(Dependencies.caffeine)
    api(Dependencies.`kotlinx-coroutines`)

    testImplementation(tck)
}