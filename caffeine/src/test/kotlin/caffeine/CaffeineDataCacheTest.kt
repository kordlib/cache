package caffeine

import dev.kord.cache.api.DataCache
import dev.kord.cache.caffeine.CaffeineDataCache
import dev.kord.cache.tck.TTLDataCacheVerifier

class CaffeineDataCacheTest : TTLDataCacheVerifier() {
    override fun newCache(): DataCache = CaffeineDataCache()
}
