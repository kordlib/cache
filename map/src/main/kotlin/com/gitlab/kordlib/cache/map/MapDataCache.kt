package com.gitlab.kordlib.cache.map

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlin.reflect.KClass

class MapDataCache : DataCache {

    private val caches: MutableMap<KClass<out Any>, MapCache<out Any, out Any>> = mutableMapOf()

    override val priority: Long
        get() = 0L

    override suspend fun register(description: DataDescription<out Any, out Any>) {
        require(description.clazz !in caches) { "description already registered :$description" }
        caches[description.clazz] = MapCache(description, this)
    }

    internal fun <T : Any> getOptionally(clazz: KClass<T>): QueryBuilder<T>? = when {
        caches.containsKey(clazz) -> query(clazz)
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> query(clazz: KClass<T>): QueryBuilder<T> =
            caches[clazz]?.query() as? QueryBuilder<T>
                    ?: error("class not registered ${clazz.java}")

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> put(item: T) {
        val cache = caches[item::class] as? MapCache<Any, Any>
                ?: error("class not registered ${item::class}")

        cache.put(item)
    }

}
