package dev.kord.cache.api.delegate


import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.data.DataDescription
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias Supplier<T> = (cache: DataCache, description: DataDescription<T, *>) -> DataEntryCache<T>

interface EntrySupplier {

    suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T>

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(supply: (cache: DataCache, description: DataDescription<Any, Any>) -> DataEntryCache<*>) = object : EntrySupplier {
            override suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T> =
                    supply(cache, description as DataDescription<Any, Any>) as DataEntryCache<T>
        }
    }

}

/**
 * A cache that delegates all operations to [DataEntryCaches][DataEntryCache], who are lazily loaded on [register].
 */
class DelegatingDataCache(private val supplier: EntrySupplier) : DataCache {

    private val caches = ConcurrentHashMap<KType, DataEntryCache<Any>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? {
        return caches[type] as? DataEntryCache<T>
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(description: DataDescription<out Any, out Any>) {
        caches[description.type] = supplier.supply(this, description as DataDescription<Any, Any>)
    }

    companion object {

        private class DelegateSupplier(private val default: Supplier<Any>, private val suppliers: MutableMap<KType, Supplier<*>>) : EntrySupplier {

            @Suppress("UNCHECKED_CAST")
            override suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T> {
                val supplier = (suppliers[description.type] ?: default) as Supplier<T>
                return supplier(cache, description)
            }
        }

        class Builder {

            val suppliers: MutableMap<KType, Supplier<*>> = mutableMapOf()
            private var default: Supplier<Any> = { _, _ -> DataEntryCache.none() }

            @Suppress("UNCHECKED_CAST")
            inline fun <reified T : Any> forType(noinline supplier: Supplier<T>) {
                suppliers[typeOf<T>()] = supplier as Supplier<*>
            }

            fun default(supplier: Supplier<Any>) {
                default = supplier
            }

            fun build(): DataCache = DelegatingDataCache(DelegateSupplier(default, suppliers))
        }

        /**
         * Creates a new [DataCache] configured by [builder].
         * [Builder.default] will use a [DataEntryCache.none] to store entries.
         */
        inline operator fun invoke(builder: Builder.() -> Unit = {}) = Builder().apply(builder).build()
    }

}
