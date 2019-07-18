package com.gitlab.kord.cache.map.query

import com.gitlab.kord.cache.api.Query
import com.gitlab.kord.cache.api.data.DataDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@ExperimentalCoroutinesApi
internal class MapQuery<KEY, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        private val descriptor: DataDescriptor<VALUE, KEY>,
        private val keyQuery: (Map<KEY, VALUE>) -> Flow<VALUE>,
        private val queries: List<(VALUE) -> Boolean>
) : Query<VALUE> {

    override suspend fun asFlow(): Flow<VALUE> = keyQuery.invoke(map).filter { value -> queries.all { it(value) } }

    override suspend fun remove() = asFlow().collect {
        map.remove(descriptor.indexField.property.get(it))
    }

}