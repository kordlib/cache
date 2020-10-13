package com.gitlab.kordlib.cache.api

import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import mu.KLogger
import mu.KotlinLogging
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal val logger = KotlinLogging.logger("DataCache")

interface DataCache {

    /**
     * Registers a [description] and preforms the necessary setup to use the type of this class.
     */
    suspend fun register(description: DataDescription<out Any, out Any>)

    /**
     * Registers the [descriptions] and preforms the necessary setup to use the type of these classes.
     */
    suspend fun register(vararg descriptions: DataDescription<out Any, out Any>) =
            descriptions.forEach { register(it) }

    /**
     * Registers the [descriptions] and preforms the necessary setup to use the type of these classes.
     */
    suspend fun register(descriptions: Iterable<DataDescription<out Any, out Any>>) =
            descriptions.forEach { register(it) }

    /**
     * Returns a [DataEntryCache] of the given [type] if its [description][DataDescription] was registered beforehand, null otherwise.
     */
    fun <T : Any> getEntry(type: KType): DataEntryCache<T>?

    companion object {
        private val empty = object : DataCache {
            override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? = DataEntryCache.none()
            override suspend fun register(description: DataDescription<out Any, out Any>) {}
        }

        /**
         * Returns an empty immutable cache. This cache will always return an empty [DataEntryCache] on [getEntry] even if no
         * [description][DataDescription] of that type was [registered][register].
         */
        @ExperimentalCoroutinesApi
        fun none() = empty
    }
}

/**
 * Returns a [DataEntryCache] of the given [T] if its [description][DataDescription] was registered beforehand, null otherwise.
 */
inline fun <reified T : Any> DataCache.getEntry(): DataEntryCache<T>? = getEntry(typeOf<T>())

/**
 * Inserts a new [item] into the cache. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
suspend fun <T : Any> DataCache.put(type: KType, item: T) = getEntry<T>(type)?.put(item)

/**
 * Inserts a new [item] into the cache. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
suspend inline fun <reified T : Any> DataCache.put(item: T) = put(typeOf<T>(), item)

/**
 * Inserts all [items] into the cache. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
suspend inline fun <reified T : Any> DataCache.putAll(items: Iterable<T>) = getEntry<T>()?.put(items)

/**
 * Inserts all [items] into the cache. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
suspend inline fun <reified T : Any> DataCache.putAll(vararg items: T) = getEntry<T>()?.put(*items)

/**
 * Inserts all [items] into the cache. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
suspend inline fun <reified T : Any> DataCache.putAll(items: Flow<T>) = getEntry<T>()?.put(items)


/**
 * Creates a new [Query] configured with the [block].
 */
@Deprecated("use query instead", ReplaceWith("query<T>(block)"), DeprecationLevel.WARNING)
inline fun <reified T : Any> DataCache.find(@BuilderInference block: QueryBuilder<T>.() -> Unit = {}): Query<T> {
    val entry = getEntry<T>()

    if(entry == null) {
        logger.debug { "entry cache for ${typeOf<T>()} was not registered. Consider registering the type via DataCache#register." }
        return Query.none()
    }

    return entry.query().apply(block).build()
}

/**
 * Creates a new [Query] configured with the [block].
 */
inline fun <reified T : Any> DataCache.query(@BuilderInference block: QueryBuilder<T>.() -> Unit = {}): Query<T> {
    val entry = getEntry<T>()

    if(entry == null) {
        logger.debug { "entry cache for ${typeOf<T>()} was not registered. Consider registering the type via DataCache#register" }
        return Query.none()
    }

    return entry.query().apply(block).build()
}

/**
 * Removes all the values that match the [block].
 */
suspend inline fun <reified T : Any> DataCache.remove(@BuilderInference block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).remove()

/**
 * Returns the amount of values that match the [block].
 */
suspend inline fun <reified T : Any> DataCache.count(@BuilderInference block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).count()


/**
 * Executes a query with the [block] and returns the values as a [Flow].
 */
inline fun <reified T : Any> DataCache.flow(@BuilderInference block: QueryBuilder<T>.() -> Unit = {}) =
        query(block).asFlow()
