package dev.kord.cache.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.cache.api.DataCache
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.map.MapLikeCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow

object CaffeineDataCache {

    /**
     * Creates a new [DataCache] configured by [builder].
     * [MapDataCache.Builder.default] will use [Caffeine] to store entries.
     */
    inline operator fun invoke(builder: MapDataCache.Builder.() -> Unit = {}) =
            MapDataCache.Builder().apply { default { caffeine() } }.apply(builder).build()
}

fun <KEY : Any, VALUE : Any> MapLikeCollection.Companion.caffeine(generator: (Caffeine<Any, Any>) -> Caffeine<Any, Any> = { it }): MapLikeCollection<KEY, VALUE> {
    return object : MapLikeCollection<KEY, VALUE> {
        val caffeine = Caffeine.newBuilder().let(generator).build<KEY, VALUE>()

        override suspend fun get(key: KEY): VALUE? = caffeine.getIfPresent(key)

        override suspend fun contains(key: KEY): Boolean = caffeine.getIfPresent(key) != null

        override suspend fun put(key: KEY, value: VALUE) = caffeine.put(key, value)

        override fun values(): Flow<VALUE> = flow {
            for (value in caffeine.asMap().values) {
                emit(value)
            }
        }

        override suspend fun clear() = caffeine.invalidateAll()

        override suspend fun remove(key: KEY) {
            caffeine.invalidate(key)
        }

        override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
            for ((key, value) in caffeine.asMap()) {
                if (predicate(key)) {
                    emit(value)
                }
            }
        }

        override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = values().filter { predicate(it) }
    }

}
