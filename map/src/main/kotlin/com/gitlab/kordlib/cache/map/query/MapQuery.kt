package com.gitlab.kordlib.cache.map.query

import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.map.MapDataCache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@ExperimentalCoroutinesApi
internal class MapQuery<KEY: Any, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        description: DataDescription<VALUE, KEY>,
        holder: MapDataCache,
        private val keyQuery: (Map<KEY, VALUE>) -> Flow<VALUE>,
        private val queries: List<(VALUE) -> Boolean>
) : CascadingQuery<VALUE>(description, holder) {

    override suspend fun asFlow(): Flow<VALUE> = keyQuery.invoke(map).filter { value -> queries.all { it(value) } }

    override suspend fun remove() = asFlow().collect { value ->
        cascadeForValue(value)
        map.remove(description.indexField.property.get(value))
    }

}