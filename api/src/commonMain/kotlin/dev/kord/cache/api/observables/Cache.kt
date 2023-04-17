package dev.kord.cache.api.observables

import kotlinx.coroutines.flow.Flow

/**
 * A cache for a map of entries where each [Value] is associated with a unique [Key].
 * */
interface Cache<Key : Any, Value : Any> {

    /**
     * Returns the value associated with the given [key] in the cache, or null if no such entry exists.
     */
    suspend fun get(key: Key): Value?

    /**
     * Removes the entry associated with the given [key] from the cache, if it exists.
     */
    suspend fun remove(key: Key)

    /**
     * Filters [Value]s by the given [predicate]
     * Returns a lazy [Flow] of [Value]s
     */
    suspend fun filter(predicate: (Value) -> Boolean): Sequence<Value>

    /**
     * Sets the [value] into the cache associated with a [key].
     * @param key The key to associate the [value] with.
     * @param value The value to be cached.
     */
    suspend fun set(key: Key, value: Value)

    /**
     * Returns the first value that satisfies the given [predicate] function, or null if none is found.
     * @param predicate A function that takes a [Value] and returns a boolean.
     * @return The first value that satisfies the [predicate] function, or null if none is found.
     */
    public suspend fun firstOrNull(predicate: (Value) -> Boolean): Value?

    /**
     * Removes any entry in the cache which matches the given [predicate].
     * @param predicate The predicate to match each [Value] against.
     */
    public suspend fun removeAny(predicate: (Value) -> Boolean)

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
    public suspend fun <R: Any> relatesTo(other: Cache<*, R>, handler: RelationHandler<Value, R>)

}
