package dev.kord.cache.map.internal

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.cache.api.QueryBuilder
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.map.MapLikeCollection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlin.reflect.KProperty1

internal class KeyValueQueryBuilder<KEY, VALUE : Any>(
        private val cache: DataCache,
        private val collection: MapLikeCollection<KEY, VALUE>,
        private val description: DataDescription<VALUE, KEY>
) : QueryBuilder<VALUE> {

    private val actions = mutableListOf<Action<KEY, VALUE>>()

    private val KProperty1<VALUE, *>.isPrimary get() = description.indexField.property == this

    @Suppress("UNCHECKED_CAST")
    override infix fun <R> KProperty1<VALUE, R>.eq(value: R) = when {
        isPrimary -> addIds(setOf(value as KEY))
        else -> predicate { it == value }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R> KProperty1<VALUE, R>.`in`(items: Iterable<R>) = when {
        isPrimary -> when (items) {
            is ClosedRange<*> -> predicate { it in items }
            else -> addIds(items as Iterable<KEY>)
        }
        else -> predicate { it in items }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R> KProperty1<VALUE, R>.ne(value: R) = when {
        isPrimary -> addNotId(value as KEY)
        else -> predicate { it != value }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R> KProperty1<VALUE, R>.notIn(items: Iterable<R>) = when {
        isPrimary -> addNotId(items as Iterable<KEY>)
        else -> predicate { it !in items }
    }

    override infix fun <R> KProperty1<VALUE, R>.predicate(predicate: (R) -> Boolean) {
        actions += Action.ValueAction { predicate(get(it)) }
    }

    @ExperimentalCoroutinesApi
    override fun build(): Query<VALUE> = ActionQuery(cache, collection, description, actions.sortedBy { it.impact })

    private fun addIds(keys: Iterable<KEY>) {
        val index = actions.indexOfFirst { it is Action.SelectByIds }

        if (index == -1) {
            actions += Action.SelectByIds(keys.toSet())
            return
        }

        val action = actions.removeAt(index) as Action.SelectByIds
        actions += Action.SelectByIds(action.keys intersect keys)
    }

    private fun addNotId(keys: Iterable<KEY>) {
        val index = actions.indexOfFirst { it is Action.SelectByIdNotPresent }

        if (index == -1) {
            actions += Action.SelectByIdNotPresent(keys.toList())
            return
        }

        val action = actions.removeAt(index) as Action.SelectByIdNotPresent<KEY, VALUE>
        actions += Action.SelectByIdNotPresent(keys + action.keys)
    }

    private fun addNotId(key: KEY) {
        val index = actions.indexOfFirst { it is Action.SelectByIdNotPresent }

        if (index == -1) {
            actions += Action.SelectByIdNotPresent(listOf(key))
            return
        }

        val action = actions.removeAt(index) as Action.SelectByIdNotPresent<KEY, VALUE>
        actions += Action.SelectByIdNotPresent(action.keys + key)
    }
}

internal abstract class Action<KEY, VALUE : Any>(val impact: Int) {

    abstract fun onMap(description: DataDescription<VALUE, KEY>, collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE>

    abstract suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean

    class ValueAction<KEY, VALUE : Any>(val action: (VALUE) -> Boolean) : Action<KEY, VALUE>(0) {
        override fun onMap(description: DataDescription<VALUE, KEY>, collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE> =
                collection.values().filter { action(it) }

        override suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean = action(value)
    }

    class SelectByIds<KEY, VALUE : Any>(val keys: Set<KEY>) : Action<KEY, VALUE>(50) {
        override fun onMap(description: DataDescription<VALUE, KEY>, collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE> =
                keys.asFlow().mapNotNull { collection.get(it) }

        override suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean =
                description.indexField.property.get(value) in keys
    }

    class SelectByIdNotPresent<KEY, VALUE : Any>(val keys: List<KEY>) : Action<KEY, VALUE>(100) {
        override fun onMap(description: DataDescription<VALUE, KEY>, collection: MapLikeCollection<KEY, VALUE>): Flow<VALUE> = flow {
            val flow = if (keys.all { collection.get(it) == null }) collection.values()
            else collection.values().filter { description.indexField.property.get(it) !in keys }

            @Suppress("EXPERIMENTAL_API_USAGE")
            emitAll(flow)
        }

        override suspend fun filter(description: DataDescription<VALUE, KEY>, value: VALUE): Boolean =
                description.indexField.property.get(value) !in keys
    }
}
