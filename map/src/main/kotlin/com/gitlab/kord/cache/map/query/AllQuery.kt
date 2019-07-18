package com.gitlab.kord.cache.map.query

import com.gitlab.kord.cache.api.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

internal class AllQuery<KEY, VALUE : Any>(private val map: MutableMap<KEY, VALUE>) : Query<VALUE> {

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

    override suspend fun remove() = map.clear()

}