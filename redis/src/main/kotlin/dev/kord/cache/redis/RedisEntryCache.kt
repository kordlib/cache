package dev.kord.cache.redis

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import dev.kord.cache.redis.internal.builder.QueryInfo
import dev.kord.cache.redis.internal.builder.RedisQueryBuilder
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.toUtf8Bytes

class RedisEntryCache<T : Any, I>(
        cache: DataCache,
        description: DataDescription<T, I>,
        configuration: RedisConfiguration,
        val serializer: KSerializer<T>,
        keySerializer: (I) -> ByteArray = { "${configuration.prefix}$it".toUtf8Bytes() },
        entryName: String = description.type.toString()
) : DataEntryCache<T> {
    private val info: QueryInfo<T, I> = QueryInfo(
            entryName = entryName.toUtf8Bytes(),
            description = description,
            binarySerializer = configuration.binaryFormat,
            cache = cache,
            commands = configuration.connection.reactive(),
            keySerializer = keySerializer,
            valueSerializer = serializer
    )


    override fun query(): QueryBuilder<T> = RedisQueryBuilder(info)

    override suspend fun put(item: T) {
        val key = info.keySerializer(info.description.indexField.property.get(item))
        val value = info.binarySerializer.dump(info.valueSerializer, item)
        info.commands.hset(info.entryName, key, value).awaitSingle()
    }

}
