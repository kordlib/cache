import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.kotlin.dsl.*

fun DokkaTask.configure(additional: DokkaTask.() -> Unit = {}) {
    this.outputDirectory.set(project.file("${project.projectDir}/dokka/kord/"))

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory = project.file("src/main/kotlin")
            remoteUrl = project.uri("https://github.com/kordlib/kord/tree/master/${project.name}/src/$name/kotlin/").toURL()

            remoteLineSuffix = "#L"
        }
    }

    additional()
}
