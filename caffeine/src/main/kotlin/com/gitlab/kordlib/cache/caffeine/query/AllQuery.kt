package com.gitlab.kordlib.cache.caffeine.query

import com.github.benmanes.caffeine.cache.Cache
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.caffeine.CaffeineDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
internal class AllQuery<KEY : Any, VALUE : Any>(
        private val cache: Cache<KEY, VALUE>,
        private val description: DataDescription<VALUE, KEY>,
        holder: CaffeineDataCache
) : CascadingQuery<VALUE>(description, holder) {
    override suspend fun asFlow(): Flow<VALUE> = cache.asMap().values.asFlow()

    override suspend fun toCollection(): Collection<VALUE> = cache.asMap().values

    override suspend fun count(): Long = cache.estimatedSize()

    override suspend fun remove() {
        cascadeAll()
        cache.invalidateAll()
    }

    override suspend fun update(mapper: suspend (VALUE) -> VALUE) {
        asFlow().collect {
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
}