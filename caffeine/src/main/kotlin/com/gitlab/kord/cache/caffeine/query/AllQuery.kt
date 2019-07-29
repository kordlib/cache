package com.gitlab.kord.cache.caffeine.query

import com.gitlab.kord.cache.api.data.DataDescription
import com.gitlab.kord.cache.caffeine.CaffeineDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@ExperimentalCoroutinesApi
internal class AllQuery<VALUE : Any>(
        private val cache: com.github.benmanes.caffeine.cache.Cache<*, VALUE>,
        description: DataDescription<VALUE, out Any>,
        holder: CaffeineDataCache
) : CascadingQuery<VALUE>(description, holder) {
    override suspend fun asFlow(): Flow<VALUE> = cache.asMap().values.asFlow()

    override suspend fun toCollection(): Collection<VALUE> = cache.asMap().values

    override suspend fun count(): Long = cache.estimatedSize()

    override suspend fun remove() {
        cascadeAll()
        cache.invalidateAll()
    }
}