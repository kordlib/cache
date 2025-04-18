import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.jetbrains.dokka.gradle.DokkaExtension

fun DokkaExtension.configure(project: Project, additional: DokkaExtension.() -> Unit = {}) {
    this.basePublicationsDirectory.set(project.file("${project.projectDir}/dokka/kord/"))

    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory = project.file("src/main/kotlin")
            remoteUrl = project.uri("https://github.com/kordlib/kord/tree/master/${project.name}/src/$name/kotlin/")

            remoteLineSuffix = "#L"
        }
    }

    additional()
}
