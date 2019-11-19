package com.gitlab.kordlib.cache.map

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.api.delegate.DelegatingDataCache
import com.gitlab.kordlib.cache.api.delegate.EntrySupplier
import com.gitlab.kordlib.cache.map.internal.MapEntryCache
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
fun DelegatingDataCache.Companion.fromMapLike(
        priority: Long = 0,
        supplier: (description: DataDescription<out Any, out Any>) -> MapLikeCollection<*, out Any>
) = DelegatingDataCache(EntrySupplier { cache, description ->
    MapEntryCache(cache, description, supplier(description) as MapLikeCollection<Any, Any>)
}, priority)

@Suppress("UNCHECKED_CAST")
fun DelegatingDataCache.Companion.fromMap(
        priority: Long = 0,
        supplier: (description: DataDescription<out Any, out Any>) -> MutableMap<*, out Any>
) = DelegatingDataCache(EntrySupplier { cache, description ->
    MapEntryCache(cache, description, MapLikeCollection.from(supplier(description) as MutableMap<Any, Any>))
}, priority)

@Suppress("FunctionName")
fun MapDataCache(priority: Long = 0): DataCache = DelegatingDataCache(EntrySupplier.invoke { cache, description ->
    MapEntryCache(cache, description, MapLikeCollection.fromThreadSafe(ConcurrentHashMap()))
}, priority)
