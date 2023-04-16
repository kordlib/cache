package dev.kord.cache.api.observables

import co.touchlab.stately.collections.ConcurrentMutableMap

/**
 * An implementation of the [EntryCache] interface that uses an index system based on the [IndexFactory] provided.
 * This implementation is thread-safe.
 *
 * @param relation The relation between this cache and other caches, used to remove related entries.
 */
public class IndexCache<Key: Any, Value : Any>(
    public val relation: Relation<Value>,
) : MapEntryCache<Key, Value> {

    private val source: ConcurrentMutableMap<Key, Value> = ConcurrentMutableMap()

    /**
     * Gets the value associated with the given [key].
     *
     * @return The value associated with the given [key], or `null` if not found.
     */
    override suspend fun get(key: Key): Value? {
        return source[key]
    }

    /**
     * Gets the first value that matches the [transform] function.
     *
     * @return The first value that matches the [transform] function, or `null` if not found.
     */
    override suspend fun firstOrNull(transform: (Value) -> Boolean): Value? {
        return source.values.find(transform)
    }

    /**
     * removes all values that match the [transform] function.
     *
     * @param transform The function used to determine which values to remove.
     */
    override suspend fun removeIf(transform: (Value) -> Boolean) {
        val entry = source.entries.find { (_, value) ->  transform(value) } ?: return
        relation.remove(entry.value)
        source.remove(entry.key)
    }


    /**
     * removes the value associated with the given [key].
     *
     * @return The value associated with the given [key], or `null` if not found.
     */
    override suspend fun remove(key: Key) {
        source.remove(key)
    }

    override suspend fun put(key: Key, value: Value) {
        source[key] = value
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
    override suspend fun getAll(): Map<Key, Value> {
        return source
    }
}