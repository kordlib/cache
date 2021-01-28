@file:Suppress("EXPERIMENTAL_API_USAGE")

package caffeine

import dev.kord.cache.tck.DataCacheVerifier
import dev.kord.cache.api.DataCache
import dev.kord.cache.caffeine.CaffeineDataCache
import org.junit.jupiter.api.BeforeEach

class CaffeineDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = CaffeineDataCache()
    }


}