val caffeineVersion: String by extra

dependencies {
    api(project(":api"))
    api (project(":map"))
    api("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
}