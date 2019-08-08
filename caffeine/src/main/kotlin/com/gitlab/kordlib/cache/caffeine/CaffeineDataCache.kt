package com.gitlab.kordlib.cache.caffeine

import com.github.benmanes.caffeine.cache.Caffeine
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
class CaffeineDataCache(
        private val generator: (Caffeine<Any, Any>) -> Caffeine<Any, Any> = { it }
) : DataCache {

    private val caches = mutableMapOf<KClass<out Any>, CaffeineCache<out Any, out Any>>()

    override val priority: Long
        get() = Long.MAX_VALUE - 1

    override suspend fun register(description: DataDescription<out Any, out Any>) {
        require(description.clazz !in caches) { "description already registered :$description" }
        val cache =
                CaffeineCache(description, this, Caffeine.newBuilder().let(generator).build())

        caches[description.clazz] = cache
    }


    internal fun <T : Any> getOptionally(clazz: KClass<T>): QueryBuilder<T>? = when {
        caches.containsKey(clazz) -> query(clazz)
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> query(clazz: KClass<T>): QueryBuilder<T> =
            caches[clazz]?.query() as? QueryBuilder<T>
                    ?: error("class not registered $clazz")

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> put(item: T) {
        val cache = caches[item::class] as? CaffeineCache<Any, Any>
                ?: error("class not registered ${item::class}")

        cache.put(item)
    }


}