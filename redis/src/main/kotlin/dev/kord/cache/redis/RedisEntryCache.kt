package dev.kord.cache.redis

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.QueryBuilder
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.redis.internal.builder.QueryInfo
import dev.kord.cache.redis.internal.builder.RedisQueryBuilder
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.time.Duration

enum class RedisCommand { HashSet, Set }

@OptIn(InternalSerializationApi::class)
class RedisEntryCache<T : Any, I> constructor(
        cache: DataCache,
        description: DataDescription<T, I>,
        configuration: RedisConfiguration,
        serializer: KSerializer<T> = description.klass.serializer(),
        keySerializer: (I) -> ByteArray = { "${configuration.prefix}$it".toByteArray(Charsets.UTF_8) },
        entryName: String = description.type.toString(),
        private val ttl: Long? = configuration.defaultTtl,
        private val command: RedisCommand = configuration.command
) : DataEntryCache<T> {
    private val info: QueryInfo<T, I> = QueryInfo(
            entryName = entryName.toByteArray(Charsets.UTF_8),
            description = description,
            binarySerializer = configuration.binaryFormat,
            cache = cache,
            commands = configuration.connection.reactive(),
            keySerializer = keySerializer,
            valueSerializer = serializer
    )

    override fun query(): QueryBuilder<T> = RedisQueryBuilder(info)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun put(item: T) {
        val key = info.keySerializer(info.description.indexField.property.get(item))
        val value = info.binarySerializer.encodeToByteArray(info.valueSerializer, item)

        when (command) {
            RedisCommand.HashSet -> info.commands.hset(info.entryName, key, value)
                .awaitSingle()
            RedisCommand.Set -> info.commands.set(key, value)
                .awaitSingle()
        }

        if (ttl != null) {
            info.commands.pexpire(key, ttl).awaitSingle()
        }
    }
}
