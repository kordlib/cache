/**
 * whether the process has been invoked by JitPack
 */
val isJitPack get() = "true" == System.getenv("JITPACK")

object Library {
    const val name = "cache"
    const val group = "dev.kord.cache"
    const val version = "0.5.x-SNAPSHOT"
    const val description = "Adaptable cache with query-like operations"
}

object Repo {
    const val releasesUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}

