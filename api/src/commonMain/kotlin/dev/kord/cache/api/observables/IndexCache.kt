package dev.kord.cache.api.observables

import co.touchlab.stately.collections.ConcurrentMutableMap

/**
 * An implementation of the [EntryCache] interface that uses an index system based on the [IndexFactory] provided.
 * This implementation is thread-safe.
 *
 * @param indexFactory The factory that generates indices for values stored in this cache.
 * @param relation The relation between this cache and other caches, used to remove related entries.
 */
public class IndexCache<Value : Any>(
    public val relation: Relation<Value>,
    public val indexFactory: IndexFactory<Value>
) : EntryCache<Value> {

    private val source: ConcurrentMutableMap<Index, Value> = ConcurrentMutableMap()

    /**
     * Gets the value associated with the given [key].
     *
     * @return The value associated with the given [key], or `null` if not found.
     */
    override suspend fun get(key: Index): Value? {
        return source[key]
    }

    /**
     * Gets the first value that matches the [transform] function.
     *
     * @return The first value that matches the [transform] function, or `null` if not found.
     */
    override suspend fun firstOrNull(transform: (Value) -> Boolean): Value? {
        return source.values.singleOrNull(transform)
    }

    /**
     * removes all values that match the [transform] function.
     *
     * @param transform The function used to determine which values to remove.
     */
    override suspend fun removeIf(transform: (Value) -> Boolean) {
        val value = firstOrNull(transform) ?: return
        relation.remove(value)
        val index = indexFactory(value)
        source.remove(index)
    }


    /**
     * removes the value associated with the given [index].
     *
     * @return The value associated with the given [index], or `null` if not found.
     */
    override suspend fun remove(index: Index): Value? {
        return source.remove(index)
    }

    /**
     * Adds the given [value] to the cache and returns its associated [Index].
     *
     * @return The [Index] associated with the added [value].
     */
    override suspend fun put(value: Value): Index {
        val index = indexFactory(value)
        source[index] = value
        return index
    }

    /**
     * removes all entries from this cache.
     */
    override suspend fun removeAll() {
        source.onEach { relation.remove(it.value) }
        source.clear()
    }

    override suspend fun <R : Any> relatesTo(other: EntryCache<R>, handler: RelationHandler<Value, R>) {
        relation.to(other, handler)
    }


    /**
     * Returns a defensive copy of the underlying [ConcurrentMutableMap].
     *
     * @return A defensive copy of the underlying [ConcurrentMutableMap].
     */
    override suspend fun asMap(): Map<Index, Value> {
        return source
    }
}