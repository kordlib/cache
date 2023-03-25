import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaTask

fun DokkaTask.configure(additional: DokkaTask.() -> Unit = {}) {
    this.outputDirectory.set(project.file("${project.projectDir}/dokka/kord/"))

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(project.file("src/main/kotlin"))
            remoteUrl.set(project.uri("https://github.com/kordlib/kord/tree/master/${project.name}/src/$name/kotlin/").toURL())

            remoteLineSuffix.set("#L")
        }
    }

    additional()
}
