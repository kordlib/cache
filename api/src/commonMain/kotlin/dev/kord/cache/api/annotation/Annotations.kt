package dev.kord.cache.api.annotation

/**
 * Marks an API as experimental, no guarantees for binary compatibility given.
 */
@RequiresOptIn("This API is experimental")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class CacheExperimental
