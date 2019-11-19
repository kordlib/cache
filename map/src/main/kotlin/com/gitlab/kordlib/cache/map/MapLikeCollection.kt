package com.gitlab.kordlib.cache.map

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow

interface MapLikeCollection<KEY, VALUE> {

    suspend fun get(key: KEY): VALUE?

    suspend fun put(key: KEY, value: VALUE)

    fun values(): Flow<VALUE>

    suspend fun clear()

    suspend fun remove(key: KEY)

    fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE>

    fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE>

    companion object {
        fun <KEY, VALUE : Any> from(map: MutableMap<KEY, VALUE>) = object : MapLikeCollection<KEY, VALUE> {
            override suspend fun get(key: KEY): VALUE? = map[key]

            override suspend fun put(key: KEY, value: VALUE) {
                map[key] = value
            }

            override fun values(): Flow<VALUE> = map.values.toList().asFlow()

            override suspend fun clear() = map.clear()

            override suspend fun remove(key: KEY) {
                map.remove(key)
            }

            override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
                for ((key, value) in map.entries.toList()) {
                    if (predicate(key)) {
                        emit(value)
                    }
                }
            }

            override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = flow {
                for (value in map.values.toList()) {
                    if (predicate(value)) {
                        emit(value)
                    }
                }
            }
        }

        fun <KEY, VALUE : Any> fromThreadSafe(map: MutableMap<KEY, VALUE>) = object : MapLikeCollection<KEY, VALUE> {
            override suspend fun get(key: KEY): VALUE? = map[key]

            override suspend fun put(key: KEY, value: VALUE) {
                map[key] = value
            }

            override fun values(): Flow<VALUE> = map.values.asFlow()

            override suspend fun clear() = map.clear()

            override suspend fun remove(key: KEY) {
                map.remove(key)
            }

            override fun getByKey(predicate: suspend (KEY) -> Boolean): Flow<VALUE> = flow {
                for ((key, value) in map.entries) {
                    if (predicate(key)) {
                        emit(value)
                    }
                }
            }

            override fun getByValue(predicate: suspend (VALUE) -> Boolean): Flow<VALUE> = flow {
                for (value in map.values) {
                    if (predicate(value)) {
                        emit(value)
                    }
                }
            }
        }
    }
}

fun <KEY, VALUE : Any> MutableMap<KEY, VALUE>.toMapLike() = MapLikeCollection.fromThreadSafe(this)
