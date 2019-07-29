package com.gitlab.kord.cache.map

import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.ConcurrentHashMap

internal class MapCache<KEY : Any, VALUE : Any> internal constructor(
        private val description: DataDescription<VALUE, KEY>,
        private val holder: MapDataCache
) {

    private val map: MutableMap<KEY, VALUE> = ConcurrentHashMap()

    @ExperimentalCoroutinesApi
    fun query(): QueryBuilder<VALUE> = MapQueryBuilder(map, holder, description)

    fun put(value: VALUE) {
        map[description.indexField.property.get(value)] = value
    }

}