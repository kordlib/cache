package dev.kord.cache.api.observables

/**
 * An implementation of [TypedCache] that stores a set of [EntryCache]s.
 */
public class DataCache : TypedCache {

    /**
     * The set of [EntryCache]s stored in this [TypedSetCache].
     */
    private val types: MutableSet<EntryCache<*>> = mutableSetOf()

    /**
     * Returns the [EntryCache] for the specified type, or `null` if it is not found.
     */
    private suspend fun <T : Any> getTypeOrNull(): EntryCache<T>? {
        return toSet().firstNotNullOfOrNull { it as? EntryCache<T> }
    }

    /**
     * Returns the [EntryCache] for the specified type, throwing an exception if it is not found.
     * @throws IllegalArgumentException if a cache for the specified type is not found.
     */
    override suspend fun <T : Any> getType(): EntryCache<T> {
        val instance = getTypeOrNull<T>()
        require(instance != null) { "Cache for type T not found" }
        return instance
    }

    /**
     * Adds the specified [EntryCache] to the [TypedSetCache].
     * @throws IllegalArgumentException if a cache for the same type already exists.
     */
    override suspend fun <T : Any> putCache(cache: EntryCache<T>) {
        require(getTypeOrNull<T>() == null) { "There must be only one cache of the same type" }
        types.add(cache)
    }

    /**
     * Returns a set of all [EntryCache]s stored in this [TypedSetCache].
     */
    public override suspend fun toSet(): Set<EntryCache<*>> = types.toSet()
}
