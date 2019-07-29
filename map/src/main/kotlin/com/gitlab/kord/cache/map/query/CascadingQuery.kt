package com.gitlab.kord.cache.map.query

import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.data.DataDescription
import com.gitlab.kord.cache.api.query.Query
import com.gitlab.kord.cache.map.MapDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal abstract class CascadingQuery<T : Any>(
        val description: DataDescription<T, out Any>,
        val holder: MapDataCache
) : Query<T> {

    protected suspend fun cascadeAll() {
        description.links.forEach { holder.getOptionally(it.target)?.build()?.remove() }
    }

    protected suspend fun cascadeForValue(value: T) {
        description.links.forEach {
            holder.getOptionally(it.target)?.apply { it.linkedField eq it.source(value) }?.build()?.remove()
        }
    }

}