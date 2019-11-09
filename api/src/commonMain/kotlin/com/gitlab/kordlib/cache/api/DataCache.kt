package com.gitlab.kordlib.cache.api

import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlin.reflect.KType
import kotlin.reflect.typeOf

interface DataCache : Comparable<DataCache> {

    val priority: Long

    suspend fun register(description: DataDescription<out Any, out Any>)

    suspend fun register(vararg descriptions: DataDescription<out Any, out Any>) =
            descriptions.forEach { register(it) }

    suspend fun register(descriptions: Iterable<DataDescription<out Any, out Any>>) =
            descriptions.forEach { register(it) }

    fun<T: Any> getEntry(type: KType) : DataEntryCache<T>?

    override fun compareTo(other: DataCache): Int = priority.compareTo(other.priority)

    companion object {
        fun none() = object : DataCache {
            override val priority: Long
                get() = Long.MIN_VALUE

            override suspend fun register(description: DataDescription<out Any, out Any>) {}
            override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? = DataEntryCache.none()
        }
    }
}

inline fun <reified T: Any> DataCache.getEntry() : DataEntryCache<T>? = getEntry(typeOf<T>())

suspend fun <T : Any> DataCache.put(type: KType, item: T) = getEntry<T>(type)?.put(item)

suspend inline fun <reified T : Any> DataCache.put(item: T) = put(typeOf<T>(), item)

inline fun <reified T : Any> DataCache.find(block: QueryBuilder<T>.() -> Unit = {}) =
        getEntry<T>(typeOf<T>())!!.query().apply(block).build()
