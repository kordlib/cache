package dev.kord.cache.api

import kotlinx.coroutines.flow.Flow

/**
 * A cache for a single type [VALUE].
 */
interface DataEntryCache<VALUE : Any> {

    /**
     * Creates a new [QueryBuilder] that operates on this cache.
     */
    fun query(): QueryBuilder<VALUE>

    /**
     * Inserts a new [item] into the cache. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    suspend fun put(item: VALUE)

    /**
     * Inserts the new [items] into the cache. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    suspend fun put(vararg items: VALUE) = items.forEach { put(it) }

    /**
     * Inserts the new [items] into the cache. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    suspend fun put(items: Iterable<VALUE>) = items.forEach { put(it) }

    /**
     * Inserts the new [items] into the cache. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    suspend fun put(items: Flow<VALUE>) = items.collect { put(it) }

    companion object {
        private val empty = object: DataEntryCache<Any> {
            override suspend fun put(item: Any) {}
            override fun query(): QueryBuilder<Any> = QueryBuilder.none()
        }

        /**
         * Returns an immutable cache with no items.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> none(): DataEntryCache<T> = empty as DataEntryCache<T>
    }
}

/**
 * Creates a new [Query] configured with the [block].
 */
@Deprecated("use query instead", ReplaceWith("query<T>(block)"), DeprecationLevel.WARNING)
inline fun <reified T : Any> DataEntryCache<T>.find(block: QueryBuilder<T>.() -> Unit = {}) =
        query().apply(block).build()

/**
 * Creates a new [Query] configured with the [block].
 */
inline fun <reified T : Any> DataEntryCache<T>.query(block: QueryBuilder<T>.() -> Unit = {}) =
        query().apply(block).build()

/**
 * Removes all the values that match the [block].
 */
suspend inline fun <reified T : Any> DataEntryCache<T>.remove(block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).remove()

/**
 * Returns the amount of values that match the [block].
 */
suspend inline fun <reified T : Any> DataEntryCache<T>.count(block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).count()

/**
 * Executes a query with the [block] and returns the values as a [Flow].
 */
inline fun <reified T : Any> DataEntryCache<T>.flow(block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).asFlow()
