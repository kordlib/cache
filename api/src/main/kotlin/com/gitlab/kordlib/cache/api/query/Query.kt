package com.gitlab.kordlib.cache.api.query

import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * A lazy query. Functions in this class should be handled as if they are operating on cold
 * [Flows][Flow].
 */
@ExperimentalCoroutinesApi
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

}
