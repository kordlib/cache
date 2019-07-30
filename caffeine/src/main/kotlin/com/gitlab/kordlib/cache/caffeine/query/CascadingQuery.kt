package com.gitlab.kordlib.cache.caffeine.query

import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.api.query.Query
import com.gitlab.kordlib.cache.caffeine.CaffeineDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal abstract class CascadingQuery<T : Any>(
        private val description: DataDescription<T, out Any>,
        private val holder: CaffeineDataCache
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