package dev.kord.cache.api

import dev.kord.cache.api.data.DataDescription
import kotlinx.coroutines.flow.*

/**
 * A lazy query. Functions in this class should be handled as if they are operating on cold
 * [Flows][Flow].
 */
interface Query<T : Any> {

    /**
     * Executes the query and returns the values as a [Flow].
     */
    fun asFlow(): Flow<T>

    /**
     * Removes all the values that match this query.
     */
    suspend fun remove()

    /**
     * Executes the query and returns the values as a [Collection].
     */
    suspend fun toCollection(): Collection<T> = asFlow().toList()

    /**
     * Returns the amount of values that match this query.
     */
    suspend fun count(): Long = asFlow().count().toLong()

    /**
     * Returns the only value that matches against this query, or throws an [Exception] on no or multiple matches.
     */
    suspend fun single(): T = asFlow().single()

    /**
     * Returns the only value that matches against this query if present, null if no, or throws a [Exception].
     */
    suspend fun singleOrNull(): T? = asFlow().singleOrNull()

    /**
     * Applies the [mapper] to the values that match against this query.
     * Returning a value with a different [DataDescription.indexField] value will result in an Exception being thrown.
     */
    suspend fun update(mapper: suspend (T) -> T)

    companion object {
        private val empty = object: Query<Any> {
            override fun asFlow(): Flow<Any> = emptyFlow()
            override suspend fun remove() {}
            override suspend fun update(mapper: suspend (Any) -> Any) {}
        }

        /**
         * Returns a query with no items.
         */
        @Suppress("UNCHECKED_CAST")
        fun<T: Any> none(): Query<T> = empty as Query<T>

    }

}
