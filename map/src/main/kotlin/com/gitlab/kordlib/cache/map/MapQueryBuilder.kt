package com.gitlab.kordlib.cache.map

import com.gitlab.kordlib.cache.api.query.Query
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.map.query.AllQuery
import com.gitlab.kordlib.cache.map.query.MapQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class MapQueryBuilder<KEY: Any, VALUE : Any>(
        private val map: MutableMap<KEY, VALUE>,
        private val holder: MapDataCache,
        private val description: DataDescription<VALUE, KEY>
) : QueryBuilder<VALUE> {

    private var keyQuery: ((Map<KEY, VALUE>) -> Flow<VALUE>)? = null
    private val queries: MutableList<(VALUE) -> Boolean> = mutableListOf()

    private val KProperty1<VALUE, *>.isPrimary get() = description.indexField.property == this && keyQuery == null

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
        else -> queries += { value != get(it) }
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

    override fun <R> KProperty1<VALUE, R>.predicate(predicate: (R) -> Boolean) = when {
        isPrimary -> keyQuery = { map -> map.values.asFlow().filter { predicate(get(it)) } }
        else -> queries += { predicate(get(it)) }
    }

    @ExperimentalCoroutinesApi
    override fun build(): Query<VALUE> = when {
        keyQuery == null && queries.isEmpty() -> AllQuery(map, description, holder)
        else -> MapQuery(map, description, holder, keyQuery
                ?: { it.values.asFlow() }, queries.toList())
    }

}