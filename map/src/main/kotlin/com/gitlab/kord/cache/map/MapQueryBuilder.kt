package com.gitlab.kord.cache.map

import com.gitlab.kord.cache.api.Query
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescriptor
import com.gitlab.kord.cache.map.query.AllQuery
import com.gitlab.kord.cache.map.query.MapQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class MapQueryBuilder<KEY, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        private val descriptor: DataDescriptor<VALUE, KEY>
) : QueryBuilder<VALUE> {

    private var keyQuery: ((Map<KEY, VALUE>) -> Flow<VALUE>)? = null
    private val queries: MutableList<(VALUE) -> Boolean> = mutableListOf()

    private val KProperty1<VALUE, *>.isPrimary get() = descriptor.indexField.property == this && keyQuery == null

    override fun <R> KProperty1<VALUE, R>.eq(value: R) = when {
        isPrimary -> keyQuery = { map ->
            map[value as KEY]?.let(::flowOf) ?: emptyFlow()
        }
        else -> queries += { value == get(it) }
    }

    override fun <R> KProperty1<VALUE, R>.ne(value: R) = when {
        isPrimary -> keyQuery = { map ->
            val flow = map.values.asFlow()

            if (map[value as KEY] == null) flow
            else flow.filter { value != get(it) }
        }
        else -> queries += { value == get(it) }
    }

    override fun <R> KProperty1<VALUE, R>.`in`(items: Iterable<R>) = when {
        isPrimary -> keyQuery = { map ->
            when {
                map.isEmpty() -> emptyFlow()
                items is Collection && items.isEmpty() -> emptyFlow()
                map.size == 1 -> map.values.asFlow().filter { get(it) in items }
                else -> items.asFlow().mapNotNull { map[it as KEY] }
            }
        }
        else -> queries += { get(it) in items }
    }

    override fun <R> KProperty1<VALUE, R>.predicate(predicate: (VALUE) -> Boolean) = when {
        isPrimary -> keyQuery = { map -> map.values.asFlow().filter { predicate(it) } }
        else -> queries += { predicate(it) }
    }

    @ExperimentalCoroutinesApi
    override fun build(): Query<VALUE> = when {
        keyQuery == null && queries.isEmpty() -> AllQuery(map)
        else -> MapQuery(map, descriptor, keyQuery ?: { it.values.asFlow() }, queries.toList())
    }

}