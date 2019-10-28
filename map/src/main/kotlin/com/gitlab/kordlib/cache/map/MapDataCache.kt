package com.gitlab.kordlib.cache.map

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.delegate.DelegatingDataCache
import com.gitlab.kordlib.cache.api.delegate.EntrySupplier
import com.gitlab.kordlib.cache.map.internal.MapEntryCache

@Suppress("FunctionName")
fun MapDataCache(priority: Long = 0): DataCache = DelegatingDataCache(EntrySupplier.invoke { cache, description ->
    MapEntryCache(cache, description, MapLikeCollection.from(mutableMapOf()))
}, priority)
