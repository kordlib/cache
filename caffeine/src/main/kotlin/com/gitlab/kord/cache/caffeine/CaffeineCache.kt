package com.gitlab.kord.cache.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.kord.cache.api.Cache
import com.gitlab.kord.cache.api.QueryBuilder
import com.gitlab.kord.cache.api.data.DataDescriptor
import com.gitlab.kord.cache.api.data.IndexField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class CaffeineCache<KEY : Any, VALUE : Any> private constructor(
        private val descriptor: DataDescriptor<VALUE, KEY>,
        private val cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>
) : Cache<VALUE> {

    override suspend fun put(value: VALUE) {
        cache.put(descriptor.indexField.property.get(value), value)
    }

    override suspend fun put(values: Iterable<VALUE>) {
        val map = values.asSequence().map {
            descriptor.indexField.property.get(it) to it
        }.toMap()

        cache.putAll(map)
    }

    @ExperimentalCoroutinesApi
    override fun query(): QueryBuilder<VALUE> = CaffeineQueryBuilder(cache, descriptor)

    companion object {
        inline operator fun <KEY : Any, reified VALUE : Any> invoke(
                property: KProperty1<VALUE, KEY>,
                cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE> = Caffeine.newBuilder().build()
        ): Cache<VALUE> = CaffeineCache(VALUE::class, property, cache)

        operator fun <KEY : Any, VALUE : Any> invoke(
                clazz: KClass<VALUE>,
                property: KProperty1<VALUE, KEY>,
                cache: com.github.benmanes.caffeine.cache.Cache<KEY, VALUE>
        ): Cache<VALUE> = CaffeineCache(DataDescriptor(clazz, IndexField(property)), cache)
    }

}