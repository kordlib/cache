package dev.kord.cache.redis.internal.query

import dev.kord.cache.api.Query
import dev.kord.cache.api.query
import dev.kord.cache.redis.internal.builder.QueryInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle

internal class RedisEmptyQuery<T : Any, I>(val info: QueryInfo<T, I>) : Query<T> {

    override suspend fun toCollection(): Collection<T> {
        return super.toCollection()
    }

    override suspend fun count(): Long = info.commands.hlen(info.entryName).awaitSingle()

    override fun asFlow(): Flow<T> =
            info.commands.hvals(info.entryName).map { info.binarySerializer.decodeFromByteArray(info.valueSerializer, it) }.asFlow()

    override suspend fun remove() {
        info.commands.del(info.entryName).awaitLast()
        if (info.description.links.isEmpty()) return

        info.description.links.forEach { link ->
            info.cache.getEntry<Any>(link.target)?.query { link.linkedField ne null }?.remove()
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
                        info.binarySerializer.encodeToByteArray(info.valueSerializer, new)
                ).awaitLast()
            }
        }
    }
}