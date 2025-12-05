package dev.kord.cache.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import dev.kord.cache.api.DataCache
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.map.MapLikeCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object CaffeineDataCache {

    /**
     * Creates a new [DataCache] configured by [builder].
     * [MapDataCache.Builder.default] will use [Caffeine] to store entries.
     */
    inline operator fun invoke(builder: MapDataCache.Builder.() -> Unit = {}) =
        MapDataCache.Builder().apply { default { caffeine() } }.apply(builder).build()
}

private data class ExpiringItem<T>(val ttl: Duration?, val item: T) {
    fun ttl(currentDuration: Long = 0) =
        ttl?.let { (it - currentDuration.toDuration(DurationUnit.NANOSECONDS)).inWholeNanoseconds } ?: Long.MAX_VALUE
}

fun <KEY : Any, VALUE : Any> MapLikeCollection.Companion.caffeine(generator: (Caffeine<Any, Any>) -> Caffeine<Any, Any> = { it }): MapLikeCollection<KEY, VALUE> {
    return object : MapLikeCollection<KEY, VALUE> {
        val caffeine = Caffeine.newBuilder()
            .let(generator)
            .expireAfter(object : Expiry<KEY, ExpiringItem<VALUE>> {
                override fun expireAfterCreate(
                    key: KEY,
                    value: ExpiringItem<VALUE>,
                    currentTime: Long
                ): Long = value.ttl()

                override fun expireAfterUpdate(
                    key: KEY,
                    value: ExpiringItem<VALUE>,
                    currentTime: Long,
                    currentDuration: Long
                ): Long = value.ttl(currentDuration)

                override fun expireAfterRead(
                    key: KEY,
                    value: ExpiringItem<VALUE>,
                    currentTime: Long,
                    currentDuration: Long
                ): Long = value.ttl(currentDuration)
            }).build<KEY, ExpiringItem<VALUE>>()

        override suspend fun get(key: KEY): VALUE? = caffeine.getIfPresent(key)?.item

        override suspend fun contains(key: KEY): Boolean = caffeine.getIfPresent(key) != null

        override suspend fun put(key: KEY, value: VALUE) = put(key, value, null)
        override suspend fun put(key: KEY, value: VALUE, ttl: Duration?) = caffeine.put(key, ExpiringItem(ttl, value))

        override fun values(): Flow<VALUE> = flow {
            for (value in caffeine.asMap().values) {
                emit(value.item)
            }
        }

        override suspend fun clear() = caffeine.invalidateAll()

        override suspend fun remove(key: KEY) {
            caffeine.invalidate(key)
        }

        override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
            for ((key, value) in caffeine.asMap()) {
                if (predicate(key)) {
                    emit(value.item)
                }
            }
        }

        override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = values().filter { predicate(it) }
    }

}
