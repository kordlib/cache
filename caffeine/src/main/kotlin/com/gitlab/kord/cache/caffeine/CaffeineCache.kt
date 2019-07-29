package com.gitlab.kord.cache.caffeine

import com.github.benmanes.caffeine.cache.Cache
import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class CaffeineCache<KEY : Any, VALUE : Any> constructor(
        private val description: DataDescription<VALUE, KEY>,
        private val holder: CaffeineDataCache,
        private val cache: Cache<KEY, VALUE>
) {

    fun put(value: VALUE) {
        cache.put(description.indexField.property.get(value), value)
    }

    fun put(values: Iterable<VALUE>) {
        val map = values.asSequence().map {
            description.indexField.property.get(it) to it
        }.toMap()

        cache.putAll(map)
    }

    @ExperimentalCoroutinesApi
    fun query(): QueryBuilder<VALUE> = CaffeineQueryBuilder(cache, description, holder)

}