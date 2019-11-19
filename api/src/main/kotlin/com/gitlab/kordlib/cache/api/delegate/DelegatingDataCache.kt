package com.gitlab.kordlib.cache.api.delegate

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.data.DataDescription
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType

interface EntrySupplier {

    suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T>

    companion object {
        operator fun invoke(provide: (cache: DataCache, description: DataDescription<Any, Any>) -> DataEntryCache<*>) = object : EntrySupplier {
            override suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T> =
                    provide(cache, description as DataDescription<Any, Any>) as DataEntryCache<T>
        }
    }
}

class DelegatingDataCache(
        private val supplier: EntrySupplier,
        override val priority: Long = 0
) : DataCache {

    private val caches = ConcurrentHashMap<KType, DataEntryCache<Any>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? {
        return caches[type] as? DataEntryCache<T>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(description: DataDescription<out Any, out Any>) {
        caches[description.type] = supplier.supply(this, description as DataDescription<Any, Any>)
    }

    companion object

}
