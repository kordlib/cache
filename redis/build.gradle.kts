apply(plugin = "kotlinx-serialization")

dependencies {
    api(api)
    api(map)
    api(Dependencies.lettuce)
    api(Dependencies.`kotlinx-serialization`)
    api(Dependencies.`kotlinx-serialization-protobuf`)
    api(Dependencies.`kotlinx-coroutines-reactive`)

    testImplementation(tck)
    testImplementation("it.ozimov:embedded-redis:0.7.2")

}