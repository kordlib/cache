package dev.kord.cache.redis.internal.query

import dev.kord.cache.api.Query
import dev.kord.cache.api.query
import dev.kord.cache.redis.internal.builder.QueryInfo
import dev.kord.cache.redis.internal.builder.RedisFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitLast

internal class RedisFilterQuery<T : Any, I>(
        private val info: QueryInfo<T, I>,
        private val head: RedisFilter<T, I>,
        private val tail: List<RedisFilter<T, I>>
) : Query<T> {

    override fun asFlow(): Flow<T> = tail
            .fold(head.startFlux()) { acc, redisFilter -> redisFilter.filterFlux(acc) }
            .asFlow()

    override suspend fun remove() {
        if (info.description.links.isEmpty()) {
            asFlow().collect {
                val key = info.keySerializer(info.description.indexField.property.get(it))
                info.commands.hdel(info.entryName, key).awaitLast()
            }
            return
        }

        asFlow().collect {
            val key = info.keySerializer(info.description.indexField.property.get(it))
            info.commands.hdel(info.entryName, key).awaitLast()
            info.description.links.forEach { link ->
                info.cache.getEntry<Any>(link.target)?.query {
                    link.linkedField eq link.source.get(it)
                }?.remove()
            }
        }
    }

    override suspend fun update(mapper: suspend (T) -> T) {
        asFlow().collect {
            val prevId = info.description.indexField.property.get(it)
            val prev = it
            val new = mapper(it)
            val newId = info.description.indexField.property.get(new)

            if (newId != prevId) error("identity rule violated: $prevId -> $newId")
            if (prev != new) {
                info.commands.hset(
                                info.entryName,
                                info.keySerializer(info.description.indexField.property.get(new)),
                                info.binarySerializer.dump(info.valueSerializer, new)
                        ).awaitLast()
            }
        }
    }
}