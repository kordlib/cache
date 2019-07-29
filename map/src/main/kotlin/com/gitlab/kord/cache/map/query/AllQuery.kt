package com.gitlab.kord.cache.map.query

import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.data.DataDescription
import com.gitlab.kord.cache.map.MapDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow


@ExperimentalCoroutinesApi
internal class AllQuery<KEY: Any, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        description: DataDescription<VALUE, KEY>,
        holder: MapDataCache
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

    private suspend fun removeFromLinks() {
        if (description.links.isNotEmpty()) {
            cascadeAll()
        }
    }

}