package map

import dev.kord.cache.api.DataCache
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.tck.DataCacheVerifier

class MapDataCacheTest : DataCacheVerifier() {
    override fun newCache(): DataCache = MapDataCache()

}
