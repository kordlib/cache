package com.gitlab.kord.cache.api

import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 *  A cache without keys, caches are allowed to implement their own data storage design.
 */
interface Cache<TYPE : Any> {

    /**
     * Starts a new query on this cache.
     */
    fun query(): QueryBuilder<TYPE>

    /**
     * Stores the [value] in the cache.
     */
    suspend fun put(value: TYPE)

    /**
     * Stores the [values] in the cache.
     */
    suspend fun put(values: Iterable<TYPE>) = values.forEach { put(it) }

}

/**
 * executes the given [block] on a new [QueryBuilder][Cache.query].
 */
@ExperimentalCoroutinesApi
inline fun <TYPE : Any> Cache<TYPE>.find(block: QueryBuilder<TYPE>.() -> Unit = {}): Query<TYPE> = query().apply(block).build()