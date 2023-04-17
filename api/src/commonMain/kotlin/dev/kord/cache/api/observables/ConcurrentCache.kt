package dev.kord.cache.api.observables

import co.touchlab.stately.collections.ConcurrentMutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

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
        return source.values.find(predicate)
    }

    /**
     * removes all values that match the [predicate] function.
     *
     * @param predicate The function used to determine which values to remove.
     */
    override suspend fun removeAny(predicate: (Value) -> Boolean) {
        val entry = source.entries.find { (_, value) ->  predicate(value) } ?: return
        relation.remove(entry.value)
        source.remove(entry.key)
    }

    override suspend fun filter(predicate: (Value) -> Boolean): Flow<Value> = flow {
        for(value in source.values) {
            if(predicate(value)) { emit(value) }
        }
    }



    /**
     * removes the value associated with the given [key].
     *
     * @return The value associated with the given [key], or `null` if not found.
     */
    override suspend fun remove(key: Key) {
        source.remove(key)
    }

    override suspend fun set(key: Key, value: Value) {
        source[key] = value
    }


    /**
     * removes all entries from this cache.
     */
    override suspend fun removeAll() {
        source.onEach { relation.remove(it.value) }
        source.clear()
    }

    override suspend fun <R : Any> relatesTo(other: Cache<*, R>, handler: RelationHandler<Value, R>) {
        relation.to(other, handler)
    }

}