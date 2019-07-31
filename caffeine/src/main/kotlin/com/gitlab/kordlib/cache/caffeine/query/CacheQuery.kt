package com.gitlab.kordlib.cache.caffeine.query

import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.caffeine.CaffeineDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@ExperimentalCoroutinesApi
internal class CacheQuery<KEY : Any, VALUE : Any>(
        private val cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>,
        private val keyQuery: (com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>) -> Flow<VALUE>,
        private val queries: List<(VALUE) -> Boolean> = mutableListOf(),
        private val description: DataDescription<VALUE, KEY>,
        holder: CaffeineDataCache
) : CascadingQuery<VALUE>(description, holder) {

    override suspend fun asFlow(): Flow<VALUE> = keyQuery(cache).filter { value -> queries.all { it(value) } }

    override suspend fun remove() = asFlow().collect {
        cache.invalidate(description.indexField.property.get(it))
        cascadeForValue(it)
    }

    override suspend fun update(mapper: suspend (VALUE) -> VALUE) = asFlow().collect {
        val prevId = description.indexField.property.get(it)
        val prev = it
        val new = mapper(it)
        val newId = description.indexField.property.get(new)

        if (newId != prevId) error("identity rule violated: $prevId -> $newId")
        if (prev != new) {
            cache.put(newId, new)
        }
    }

}