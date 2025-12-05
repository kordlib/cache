package dev.kord.cache.map.internal

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.DataEntryCacheWithTTL
import dev.kord.cache.api.QueryBuilder
import dev.kord.cache.api.annotation.CacheExperimental
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.map.MapLikeCollection
import kotlin.time.Duration

/**
 * a [DataEntryCache] that uses a [collection] to store and query its values.
 */
class MapEntryCache<KEY, VALUE : Any>(
        private val cache: DataCache,
        private val description: DataDescription<VALUE, KEY>,
        private val collection: MapLikeCollection<KEY, VALUE>
) : DataEntryCacheWithTTL<VALUE> {

    override suspend fun put(item: VALUE) {
        val key = description.indexField.property.get(item)
        collection.put(key, item)
    }
    @CacheExperimental
    override suspend fun put(item: VALUE, ttl: Duration?) {
        val key = description.indexField.property.get(item)
        collection.put(key, item, ttl)
    }

    override fun query(): QueryBuilder<VALUE> = KeyValueQueryBuilder(cache, collection, description)

}
