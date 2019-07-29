package com.gitlab.kord.cache.api

import com.gitlab.kord.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass

interface DataCache : Comparable<DataCache> {

    val priority: Long

    suspend fun register(description: DataDescription<out Any, out Any>)

    suspend fun register(vararg descriptions: DataDescription<out Any, out Any>) =
            descriptions.forEach { register(it) }

    suspend fun register(descriptions: Iterable<DataDescription<out Any, out Any>>) =
            descriptions.forEach { register(it) }

    fun <T : Any> query(clazz: KClass<T>): QueryBuilder<T>

    suspend fun <T : Any> put(item: T)

    override fun compareTo(other: DataCache): Int = priority.compareTo(other.priority)

    companion object {
        @ExperimentalCoroutinesApi
        fun none() = object : DataCache {
            override val priority: Long
                get() = Long.MIN_VALUE

            override suspend fun register(description: DataDescription<out Any, out Any>) {}
            override fun <T : Any> query(clazz: KClass<T>): QueryBuilder<T> = QueryBuilder.none()
            override suspend fun <T : Any> put(item: T) {}
        }
    }
}

inline fun <reified T : Any> DataCache.query() = query(T::class)

@BuilderInference
@ExperimentalCoroutinesApi
@UseExperimental(ExperimentalTypeInference::class)
inline fun <reified T : Any> DataCache.find(block: QueryBuilder<T>.() -> Unit = {}) =
        query(T::class).apply(block).build()
