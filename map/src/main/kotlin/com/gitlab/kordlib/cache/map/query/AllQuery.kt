package com.gitlab.kordlib.cache.map.query

import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.map.MapDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect


@ExperimentalCoroutinesApi
internal class AllQuery<KEY: Any, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        private val description: DataDescription<VALUE, KEY>,
        private val holder: MapDataCache
) : CascadingQuery<VALUE>(description, holder) {

    override suspend fun asFlow(): Flow<VALUE> = map.values.asFlow()

    override suspend fun count(): Long = map.size.toLong()

    override suspend fun single(): VALUE {
        val item = map.values.firstOrNull()
        if (map.size != 1 || item == null) error("one item expected")

        return item
    }

    override suspend fun singleOrNull(): VALUE? {
        val item = map.values.firstOrNull()
        if (map.size > 1) error("one item expected")

        return item
    }

    override suspend fun toCollection(): Collection<VALUE> = map.values

    override suspend fun remove() {
        removeFromLinks()
        map.clear()
    }

    override suspend fun update(mapper: suspend (VALUE) -> VALUE) {
        asFlow().collect {
            val prevId = description.indexField.property.get(it)
            val prev = it
            val new = mapper(it)
            val newId = description.indexField.property.get(new)

            if (newId != prevId) error("identity rule violated: $prevId -> $newId")
            if (prev != new) {
                map[newId] = new
            }
        }
    }

    private suspend fun removeFromLinks() {
        if (description.links.isNotEmpty()) {
            cascadeAll()
        }
    }

}