package dev.kord.cache.api.observables

/**
 * A cache for a map of entries where each [Value] is associated with a unique [Key].
 * */
interface MapEntryCache<Key : Any, Value : Any> : Cache<Value> {

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

}
