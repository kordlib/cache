package dev.kord.cache.api.observables

/**
 * A cache for a single value of type [T].
 */
public interface EntryCache<T : Any> {
    /**
     * Puts the [value] into the cache.
     * @param value The value to be cached.
     */
    public suspend fun put(value: T)

    /**
     * Returns a defensive copy of the cache entries [T].
     * @return A collection of the cached values.
     */
    public suspend fun getAll(): Collection<T>
}
