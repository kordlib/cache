package com.gitlab.kordlib.cache.map.internal

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.map.MapLikeCollection

/**
 * a [DataEntryCache] that uses a [collection] to store and query its values.
 */
class MapEntryCache<KEY, VALUE : Any>(
        private val cache: DataCache,
        private val description: DataDescription<VALUE, KEY>,
        private val collection: MapLikeCollection<KEY, VALUE>
) : DataEntryCache<VALUE> {

    override suspend fun put(item: VALUE) {
        val key = description.indexField.property.get(item)
        collection.put(key, item)
    }

    override fun query(): QueryBuilder<VALUE> = KeyValueQueryBuilder(cache, collection, description)

}
