import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

val DependencyHandlerScope.api get() = project(":api")
val DependencyHandlerScope.map get() = project(":map")
val DependencyHandlerScope.tck get() = project(":tck")

/**
 * whether the process has been invoked by JitPack
 */
val isJitPack get() = "true" == System.getenv("JITPACK")

object Library {
    private const val releaseVersion = "0.3.0-SNAPSHOT"
    val isSnapshot: Boolean get() = releaseVersion.endsWith("-SNAPSHOT")
    val isRelease: Boolean get() = !isSnapshot

    const val name = "cache"
    const val group = "dev.kord.cache"
    val version: String = if (isJitPack) System.getenv("RELEASE_TAG")
    else releaseVersion

    const val description = "Adaptable cache with query-like operations"
    const val projectUrl = "https://github.com/kordlib/cache"

    /**
     * Whether the current API is considered stable, and should be compared to the 'golden' API dump.
     */
    val isStableApi: Boolean get() = !isSnapshot
}

object Repo {
    const val releasesUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}