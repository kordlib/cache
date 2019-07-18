package com.gitlab.kord.cache.caffeine

import com.gitlab.kord.cache.api.Query
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescriptor
import com.gitlab.kord.cache.caffeine.query.AllQuery
import com.gitlab.kord.cache.caffeine.query.CacheQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty1

@ExperimentalCoroutinesApi
internal class CaffeineQueryBuilder<KEY : Any, VALUE : Any>(
        private val cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>,
        private val descriptor: DataDescriptor<VALUE, KEY>
) : QueryBuilder<VALUE> {

    private var keyQuery: ((com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>) -> Flow<VALUE>)? = null
    private val queries: MutableList<(VALUE) -> Boolean> = mutableListOf()

    private val KProperty1<VALUE, *>.isPrimary get() = descriptor.indexField.property == this && keyQuery == null

    override fun <R> KProperty1<VALUE, R>.eq(value: R) = when {
        isPrimary -> keyQuery = { cache -> cache.getIfPresent(value as KEY)?.let(::flowOf) ?: emptyFlow() }
        else -> queries += { value == get(it) }
    }

    override fun <R> KProperty1<VALUE, R>.ne(value: R) = when {
        isPrimary -> keyQuery = { cache ->
            val flow = cache.asMap().values.asFlow()

            if (cache.getIfPresent(value as KEY) == null) flow
            else flow.filter { value != get(it) }
        }
        else -> queries += { value == get(it) }
    }

    override fun <R> KProperty1<VALUE, R>.`in`(items: Iterable<R>) = when {
        isPrimary -> keyQuery = { cache -> cache.getAllPresent(items).values.asFlow() }
        else -> queries += { get(it) in items }
    }

    override fun <R> KProperty1<VALUE, R>.predicate(predicate: (VALUE) -> Boolean) = when {
        isPrimary -> keyQuery = { cache -> cache.asMap().values.asFlow().filter { predicate(it) } }
        else -> queries += { predicate(it) }
    }

    @ExperimentalCoroutinesApi
    override fun build(): Query<VALUE> = when {
        keyQuery == null && queries.isEmpty() -> AllQuery(cache)
        else -> CacheQuery(cache, descriptor, keyQuery ?: { it.asMap().values.asFlow() }, queries.toList())
    }

}