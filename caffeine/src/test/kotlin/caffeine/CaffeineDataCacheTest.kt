package caffeine

import dev.kord.cache.tck.DataCacheVerifier
import dev.kord.cache.api.DataCache
import dev.kord.cache.caffeine.CaffeineDataCache
import org.junit.jupiter.api.BeforeEach

class CaffeineDataCacheTest : DataCacheVerifier() {
    override fun newCache(): DataCache = CaffeineDataCache()
}
