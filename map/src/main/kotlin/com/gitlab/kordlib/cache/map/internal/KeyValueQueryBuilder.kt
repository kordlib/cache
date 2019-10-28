package com.gitlab.kordlib.cache.map.internal

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.api.query.Query
import com.gitlab.kordlib.cache.map.MapLikeCollection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlin.reflect.KProperty1

internal class KeyValueQueryBuilder<KEY, VALUE : Any>(
        private val cache: DataCache,
        private val collection: MapLikeCollection<KEY, VALUE>,
        private val description: DataDescription<VALUE, KEY>
) : QueryBuilder<VALUE> {

    private val actions = mutableListOf<Action<KEY, VALUE>>()

    private val KProperty1<VALUE, *>.isPrimary get() = description.indexField.property == this

    override infix fun <R> KProperty1<VALUE, R>.eq(value: R) = when {
        isPrimary -> actions += Action.KeyActionReturnsMaxOne(value as KEY)
        else -> predicate { it == value }
    }

    override infix fun <R> KProperty1<VALUE, R>.predicate(predicate: (R) -> Boolean) {
        actions += Action.ValueAction { predicate(get(it)) }
    }

    @ExperimentalCoroutinesApi
    override fun build(): Query<VALUE> = ActionQuery(cache, collection, description, actions.toList())
}

internal abstract class Action<KEY, VALUE : Any>(val impact: Int) {

    abstract fun onMap(collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE>

    abstract suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean

    class ValueAction<KEY, VALUE : Any>(val action: (VALUE) -> Boolean) : Action<KEY, VALUE>(0) {
        override fun onMap(collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE> = collection.values().filter { action(it) }
        override suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean = action(value)
    }

    class KeyActionReturnsMaxOne<KEY, VALUE : Any>(private val key: KEY) : Action<KEY, VALUE>(1) {
        override fun onMap(collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE> =
                flowOf(key).mapNotNull { collection.get(it) }

        override suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean =
                description.indexField.property.get(value) == key
    }
}
