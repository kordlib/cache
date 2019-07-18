package com.gitlab.kord.cache.caffeine.query

import com.gitlab.kord.cache.api.Query
import com.gitlab.kord.cache.api.data.DataDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@ExperimentalCoroutinesApi
internal class CacheQuery<KEY : Any, VALUE : Any>(
        private val cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>,
        private val descriptor: DataDescriptor<VALUE, KEY>,
        private val keyQuery: (com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>) -> Flow<VALUE>,
        private val queries: List<(VALUE) -> Boolean> = mutableListOf()
) : Query<VALUE> {

    override suspend fun asFlow(): Flow<VALUE> = keyQuery(cache).filter { value -> queries.all { it(value) } }

    override suspend fun remove() = asFlow().collect {
        cache.invalidate(descriptor.indexField.property.get(it))
    }

}