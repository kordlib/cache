package com.gitlab.kord.cache.caffeine.query

import com.gitlab.kord.cache.api.Query
import com.gitlab.kord.cache.api.data.DataDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@ExperimentalCoroutinesApi
internal class AllQuery<VALUE : Any>(
        private val cache: com.github.benmanes.caffeine.cache.Cache<*, VALUE>
) : Query<VALUE> {
    override suspend fun asFlow(): Flow<VALUE> = cache.asMap().values.asFlow()

    override suspend fun count(): Long = cache.estimatedSize()

    override suspend fun remove() = cache.invalidateAll()
}