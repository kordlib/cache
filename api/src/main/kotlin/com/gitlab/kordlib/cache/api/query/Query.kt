package com.gitlab.kordlib.cache.api.query

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
    suspend fun asFlow(): Flow<T>

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

}
