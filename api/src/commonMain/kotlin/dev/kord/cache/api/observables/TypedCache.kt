package dev.kord.cache.api.observables

/**
 * An interface representing a cache that can store multiple types of data by associating them with their types.
 */
public interface TypedCache {

    /**
     * Returns the [EntryCache] associated with the type [T].
     */
    public suspend fun <T : Any> getType(): EntryCache<T>


    /**
     * Adds the given [EntryCache] to this [TypedCache].
     */
    public suspend fun <T : Any> putCache(cache: EntryCache<T>)

    /**
     * Returns a [Set] of all the [EntryCache]s stored in this [TypedCache].
     */
    public suspend fun toSet(): Set<EntryCache<*>>
}
