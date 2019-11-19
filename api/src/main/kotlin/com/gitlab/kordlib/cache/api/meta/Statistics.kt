package com.gitlab.kordlib.cache.api.meta

import kotlinx.atomicfu.atomic
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class TypeStatistics(
        val type: KType,
        val queries: Long,
        val hits: Long
) {
    val misses get() = queries - hits

    val hitRatio: Double
        get() = when (queries) {
            0L -> 0.0
            else -> hits.toDouble() / queries.toDouble()
        }
}

class CacheStatistics(val typeData: Map<KType, TypeStatistics> = mapOf()) {

    val queries: Long get() = typeData.values.fold<TypeStatistics, Long>(0) { acc, descriptionMetaData -> acc + descriptionMetaData.queries }
    val hits: Long get() = typeData.values.fold<TypeStatistics, Long>(0) { acc, descriptionMetaData -> acc + descriptionMetaData.hits }
    val misses: Long get() = typeData.values.fold<TypeStatistics, Long>(0) { acc, descriptionMetaData -> acc + descriptionMetaData.misses }

    val hitRatio: Double
        get() = when (queries) {
            0L -> 0.0
            else -> hits.toDouble() / queries.toDouble()
        }

    inline fun<reified T> getForType() : TypeStatistics? = typeData[typeOf<T>()]

}

class StatisticsLogger {

    internal data class TypeStatistics(val type: KType) {
        val queries = atomic(0L)
        val hits = atomic(0L)
    }

    private val map = mutableMapOf<KType, TypeStatistics>()

    val metaData get() = CacheStatistics(map.mapValues { TypeStatistics(it.key, it.value.queries.value, it.value.hits.value) })

    fun getForType(type: KType): TypeStatisticsLogger = TypeStatisticsLogger(map.getOrPut(type) { TypeStatistics(type) })

    inline fun <reified T> getForType(): TypeStatisticsLogger = getForType(typeOf<T>())

}

class TypeStatisticsLogger internal constructor(private val data: StatisticsLogger.TypeStatistics) {

    val type get() = data.type

    fun logQuery() {
        data.queries.plusAssign(1)
    }

    fun logHit() {
        data.hits.plusAssign(1)
    }

}
