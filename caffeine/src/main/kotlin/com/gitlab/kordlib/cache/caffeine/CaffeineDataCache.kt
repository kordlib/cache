package com.gitlab.kordlib.cache.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.delegate.DelegatingDataCache
import com.gitlab.kordlib.cache.api.delegate.EntrySupplier
import com.gitlab.kordlib.cache.map.MapLikeCollection
import com.gitlab.kordlib.cache.map.internal.MapEntryCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow

@Suppress("FunctionName")
fun CaffeineDataCache(priority: Long = 0, generator: (Caffeine<Any, Any>) -> Caffeine<Any, Any> = { it }): DataCache {
    return DelegatingDataCache(EntrySupplier { cache, description ->
        MapEntryCache(cache, description, MapLikeCollection.from(generator))
    }, priority)
}

fun <KEY : Any, VALUE : Any> MapLikeCollection.Companion.from(generator: (Caffeine<Any, Any>) -> Caffeine<Any, Any>): MapLikeCollection<KEY, VALUE> {
    return object : MapLikeCollection<KEY, VALUE> {
        val caffeine = Caffeine.newBuilder().let(generator).build<KEY, VALUE>()

        override suspend fun get(key: KEY): VALUE? = caffeine.getIfPresent(key)

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
