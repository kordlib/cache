package dev.kord.cache.map

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.api.delegate.DelegatingDataCache
import dev.kord.cache.api.delegate.DelegatingDataCache.Companion.Builder
import dev.kord.cache.api.delegate.EntrySupplier
import dev.kord.cache.map.internal.MapEntryCache
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias Supplier<T> = MapLikeCollection.Companion.(description: DataDescription<T, *>) -> MapLikeCollection<out Any, T>

object MapDataCache {

    private class MapSupplier(private val default: Supplier<Any>, private val suppliers: MutableMap<KType, Supplier<*>>) : EntrySupplier {

        @Suppress("UNCHECKED_CAST")
        override suspend fun <T : Any> supply(cache: DataCache, description: DataDescription<T, out Any>): DataEntryCache<T> {
            val supplier = (suppliers[description.type] ?: default) as Supplier<T>
            val map = supplier(MapLikeCollection, description)

            return MapEntryCache(cache, description as DataDescription<T, Any>, map as MapLikeCollection<Any, T>)
        }
    }

    class Builder {

        val suppliers: MutableMap<KType, Supplier<*>> = mutableMapOf()
        private var default: Supplier<Any> = { concurrentHashMap() }

        /**
         * Assigns the [supplier] to the specific type.
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> forType(noinline supplier: Supplier<T>?) {
            if (supplier == null) suppliers.remove(typeOf<T>())
            suppliers[typeOf<T>()] = supplier as Supplier<*>
        }

        /**
         * Sets the [supplier] for types that weren't defined by [forType].
         */
        fun default(supplier: Supplier<Any>) {
            default = supplier
        }

        fun build(): DataCache = DelegatingDataCache(MapSupplier(default, suppliers))

    }

    /**
     * Creates a new [DataCache] configured by [builder].
     * [Builder.default] will use a [ConcurrentHashMap] to store entries.
     */
    inline operator fun invoke(builder: Builder.() -> Unit = {}) = Builder().apply(builder).build()

}