package dev.kord.cache.api.observables

interface Cache<T: Any> {

    /**
     * Removes any cached value [T] that satisfies the given [transform] function.
     * @param transform A function that takes a value of type [T] and returns a boolean.
     */

    /**
     * Returns the first value that satisfies the given [transform] function, or null if none is found.
     * @param transform A function that takes a value of type [T] and returns a boolean.
     * @return The first value that satisfies the [transform] function, or null if none is found.
     */
    public suspend fun firstOrNull(transform: (T) -> Boolean): T?


    public suspend fun removeIf(transform: (T) -> Boolean)

    /**
     * Removes all entries in the cache.
     */
    public suspend fun removeAll()

    /**
     * Adds an observer cache to this cache. Whenever a value is removed from this cache, the
     * observer cache will also remove any values that are related to it.
     * @param other The observer cache.
     * @param handler A [RelationHandler] that specifies how to remove related values from the observer cache.
     */
    public suspend fun <R: Any> relatesTo(other: EntryCache<R>, handler: RelationHandler<T, R>)

}