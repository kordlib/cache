package dev.kord.cache.redis.internal.builder

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.cache.api.QueryBuilder
import dev.kord.cache.api.data.DataDescription
import io.lettuce.core.api.reactive.RedisReactiveCommands
import io.lettuce.core.cluster.api.reactive.RedisClusterReactiveCommands
import dev.kord.cache.redis.internal.query.RedisEmptyQuery
import dev.kord.cache.redis.internal.query.RedisFilterQuery
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import reactor.core.publisher.Flux
import kotlin.reflect.KProperty1

internal class QueryInfo<T : Any, I>(
        val entryName: ByteArray,
        val binarySerializer: BinaryFormat,
        val valueSerializer: KSerializer<T>,
        val description: DataDescription<T, I>,
        val commands: RedisCoroutinesCommands<ByteArray, ByteArray>,
        val keySerializer: (I) -> ByteArray,
        val cache: DataCache
)


@Suppress("UNCHECKED_CAST")
internal class RedisQueryBuilder<T : Any, I>(private val info: QueryInfo<T, I>) : QueryBuilder<T> {
    private val KProperty1<*, *>.isPrimary get() = info.description.indexField.property == this

    private val identityFilters = mutableListOf<IdentityFilter<T, I>>()
    private val valueFilters = mutableListOf<ValueFilter<T, I>>()

    override fun <R> KProperty1<T, R>.eq(value: R) {
        if (isPrimary) identityFilters.add(IdentityFilter.SelectByIdFilter(info, value as I))
        else predicate { it == value }
    }

    override fun <R> KProperty1<T, R>.`in`(items: Iterable<R>) {
        if (isPrimary && items is List) identityFilters.add(IdentityFilter.SelectByIdsFilter(info, items as List<I>))
        else predicate { it in items }
    }

    override fun <R> KProperty1<T, R>.predicate(predicate: (R) -> Boolean) {
        valueFilters.add(ValueFilter.PredicateFilter(info) { predicate(get(it)) })
    }

    override fun build(): Query<T> = if (identityFilters.isEmpty() && valueFilters.isEmpty()) RedisEmptyQuery(info)
    else {
        val head: RedisFilter<T, I>
        val tail: List<RedisFilter<T, I>>
        if (identityFilters.isEmpty()) {
            head = valueFilters.first()
            tail = valueFilters.drop(1)
        } else {
            head = identityFilters.first()
            tail = identityFilters.drop(1) + valueFilters
        }

        RedisFilterQuery(info, head, tail)
    }

}
