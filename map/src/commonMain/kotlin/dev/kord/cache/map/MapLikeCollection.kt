package dev.kord.cache.map

import dev.kord.cache.api.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

interface MapLikeCollection<KEY, VALUE> {

    suspend fun get(key: KEY): VALUE?

    suspend fun contains(key: KEY): Boolean = get(key) != null

    suspend fun put(key: KEY, value: VALUE)

    suspend fun put(key: KEY, value: VALUE, ttl: Duration?): Unit =
        throw UnsupportedOperationException("This implementation does not support TTLs")

    fun values(): Flow<VALUE>

    suspend fun clear()

    suspend fun remove(key: KEY)

    fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE>

    fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE>

    companion object {
        private val empty = object : MapLikeCollection<Any, Any> {
            override suspend fun clear() {}
            override suspend fun get(key: Any): Any? = null
            override suspend fun put(key: Any, value: Any) {}
            override suspend fun remove(key: Any) {}
            override fun values(): Flow<Any> = emptyFlow()
            override fun getByKey(predicate: suspend (Any) -> Boolean): Flow<Any> = emptyFlow()
            override fun getByValue(predicate: suspend (Any) -> Boolean): Flow<Any> = emptyFlow()
        }

        /**
         * Returns an empty immutable [MapLikeCollection].
         * This collection will always return null and empty flows and ignores inserted valued.
         */
        @Suppress("UNCHECKED_CAST")
        fun <K, V : Any> none(): MapLikeCollection<K, V> = empty as MapLikeCollection<K, V>

        /**
         * Wraps the [map] in a [MapLikeCollection].
         */
        fun <K, V : Any> concurrentHashMap(map: ConcurrentHashMap<K, V> = ConcurrentHashMap()) = from(map)

        /**
         * Wraps the [map] in a [MapLikeCollection] that ignores inserted values.
         */
        fun <K, V : Any> readOnly(map: Map<K, V>) = object : MapLikeCollection<K, V> {
            override suspend fun clear() {}
            override suspend fun get(key: K): V? = map[key]
            override suspend fun put(key: K, value: V) {}
            override suspend fun remove(key: K) {}
            override fun values(): Flow<V> = flow {
                map.values.forEach { emit(it) }
            }

            override fun getByKey(predicate: suspend (K) -> Boolean): Flow<V> = flow {
                for ((key, value) in map.entries.toList()) if (predicate(key)) emit(value)
            }

            override fun getByValue(predicate: suspend (V) -> Boolean): Flow<V> = flow {
                for (value in map.values.toList()) if (predicate(value)) emit(value)
            }
        }

        /**
         * Wraps the [map] in a [MapLikeCollection], data will be copied on read to prevent modification exceptions.
         */
        fun <KEY, VALUE : Any> from(map: MutableMap<KEY, VALUE>) = object : MapLikeCollection<KEY, VALUE> {
            override suspend fun get(key: KEY): VALUE? = map[key]

            override suspend fun contains(key: KEY): Boolean = map.contains(key)

            override suspend fun put(key: KEY, value: VALUE) {
                map[key] = value
            }

            override fun values(): Flow<VALUE> = flow {
                ArrayList(map.values).forEach { emit(it) }
            }

            override suspend fun clear() = map.clear()

            override suspend fun remove(key: KEY) {
                map.remove(key)
            }

            override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
                for ((key, value) in map.entries.toList()) {
                    if (predicate(key)) {
                        emit(value)
                    }
                }
            }

            override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = flow {
                for (value in map.values.toList()) {
                    if (predicate(value)) {
                        emit(value)
                    }
                }
            }
        }

        /**
         * Wraps the [map] in a [MapLikeCollection], this assumes that the [map] can safely be accessed concurrently.
         */
        fun <KEY, VALUE : Any> fromThreadSafe(map: MutableMap<KEY, VALUE>) = object : MapLikeCollection<KEY, VALUE> {
            override suspend fun get(key: KEY): VALUE? = map[key]

            override suspend fun contains(key: KEY): Boolean =
                map.contains(key)

            override suspend fun put(key: KEY, value: VALUE) {
                map[key] = value
            }

            override fun values(): Flow<VALUE> = flow {
                map.values.forEach { emit(it) }
            }

            override suspend fun clear() = map.clear()

            override suspend fun remove(key: KEY) {
                map.remove(key)
            }

            override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
                for ((key, value) in map.entries) {
                    if (predicate(key)) {
                        emit(value)
                    }
                }
            }

            override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = flow {
                for (value in map.values) {
                    if (predicate(value)) {
                        emit(value)
                    }
                }
            }
        }
    }
}

/**
 * Wraps the [map] in a [MapLikeCollection], this assumes that the [map] can safely be accessed concurrently if [concurrent] is `true`.
 */
fun <KEY, VALUE : Any> MutableMap<KEY, VALUE>.toMapLike(concurrent: Boolean) = when (concurrent) {
    true -> MapLikeCollection.fromThreadSafe(this)
    false -> MapLikeCollection.from(this)
}
