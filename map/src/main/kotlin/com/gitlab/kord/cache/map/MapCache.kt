package com.gitlab.kord.cache.map

import com.gitlab.kord.cache.api.Cache
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescriptor
import com.gitlab.kord.cache.api.data.IndexField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MapCache<KEY, VALUE : Any> private constructor(
        private val descriptor: DataDescriptor<VALUE, KEY>,
        persistent: Boolean = true
) : Cache<VALUE> {

    private val map: MutableMap<KEY, VALUE> = when {
        persistent -> ConcurrentHashMap()
        else -> Collections.synchronizedMap(WeakHashMap())
    }

    @ExperimentalCoroutinesApi
    override fun query(): QueryBuilder<VALUE> = MapQueryBuilder(map, descriptor)

    override suspend fun put(value: VALUE) {
        map[descriptor.indexField.property.get(value)] = value
    }

    companion object {
        inline operator fun <KEY, reified VALUE : Any> invoke(
                property: KProperty1<VALUE, KEY>,
                persistent: Boolean = true
        ): Cache<VALUE> = MapCache(VALUE::class, property, persistent)

        operator fun <KEY, VALUE : Any> invoke(
                clazz: KClass<VALUE>,
                property: KProperty1<VALUE, KEY>,
                persistent: Boolean = true
        ): Cache<VALUE> = MapCache(DataDescriptor(clazz, IndexField(property)), persistent)
    }

}