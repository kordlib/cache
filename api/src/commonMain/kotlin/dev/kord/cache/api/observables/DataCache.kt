package dev.kord.cache.api.observables

/**
 * A cache for a map of entries where each [Value] is associated with a unique [Key].
 * */
interface DataCache<Key : Any, Value : Any> {

    /**
     * Returns the value associated with the given [key] in the cache, or null if no such entry exists.
     */
    suspend fun get(key: Key): Value?

    /**
     * Removes the entry associated with the given [key] from the cache, if it exists.
     */
    suspend fun remove(key: Key)

    /**
     * Returns a [Map] containing all entries in the cache.
     */
    suspend fun getAll(): Map<Key, Value>

    /**
     * Puts the [value] into the cache associated with a [key].
     * @param key The key to associate the [value] with.
     * @param value The value to be cached.
     */
    public suspend fun put(key: Key, value: Value)


    /**
     * Removes any cached value [T] that satisfies the given [transform] function.
     * @param transform A function that takes a value of type [T] and returns a boolean.
     */

    /**
     * Returns the first value that satisfies the given [transform] function, or null if none is found.
     * @param transform A function that takes a value of type [T] and returns a boolean.
     * @return The first value that satisfies the [transform] function, or null if none is found.
     */
    public suspend fun firstOrNull(predicate: (Value) -> Boolean): Value?


    public suspend fun removeIf(predicate: (Value) -> Boolean)

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
    public suspend fun <R: Any> relatesTo(other: DataCache<*, R>, handler: RelationHandler<Value, R>)

}
