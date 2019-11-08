import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.3.50" apply false
    kotlin("kapt") version "1.3.50" apply false
}


repositories {
    mavenCentral()
    jcenter()
}

subprojects {
    group = "com.gitlab.kordlib.cache"
    version = "0.0.1"

    apply(from = "../config.gradle.kts")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        testImplementation(project(":tck"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
        testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf(
                "-XXLanguage:+InlineClasses",
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlin.ExperimentalStdlibApi",
                "-Xuse-experimental=kotlin.experimental.ExperimentalTypeInference"
        )
    }
}
