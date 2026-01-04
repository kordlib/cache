package dev.kord.cache.api.observables

import co.touchlab.stately.collections.ConcurrentMutableMap

/**
 * An implementation of the [Cache] with [ConcurrentMutableMap].
 * This implementation is thread-safe.
 *
 * @param relation The relation between this cache and other caches, used to remove related entries.
 */
public class ConcurrentCache<Key: Any, Value : Any>(
    public val relation: Relation<Value>,
) : Cache<Key, Value> {

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
     * Gets the first value that matches the [predicate] function.
     *
     * @return The first value that matches the [predicate] function, or `null` if not found.
     */
    override suspend fun firstOrNull(predicate: (Value) -> Boolean): Value? {
        return source.values.firstOrNull(predicate)
    }

    /**
     * removes all values that match the [predicate] function.
     *
     * @param predicate The function used to determine which values to remove.
     */
    override suspend fun removeAny(predicate: (Value) -> Boolean) {
        source.asSequence()
            .filter { (_, value) -> predicate(value) }
            .forEach { (key, _) -> remove(key) }
    }

    override suspend fun filter(predicate: (Value) -> Boolean): Sequence<Value> =
        source.asSequence()
            .filter { (_, value) -> predicate(value) }
            .map { it.value }


    /**
     * removes the value associated with the given [key].
     *
     * @return The value associated with the given [key], or `null` if not found.
     */
    override suspend fun remove(key: Key) {
        val value = source[key] ?: return
        relation.remove(value)
        source.remove(key)
    }

    override suspend fun set(key: Key, value: Value) {
        source[key] = value
    }


    /**
     * removes all entries from this cache.
     */
    override suspend fun removeAll() {
        val iterator = source.iterator()
        while (iterator.hasNext()) {
            val (_, value) = iterator.next()
            relation.remove(value)
            iterator.remove()
        }
    }


    override suspend fun <R : Any> relatesTo(other: Cache<*, R>, handler: RelationHandler<Value, R>) {
        relation.to(other, handler)
    }

}