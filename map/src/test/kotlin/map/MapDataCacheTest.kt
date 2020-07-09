package map

import dev.kord.cache.tck.DataCacheVerifier
import dev.kord.cache.api.DataCache
import dev.kord.cache.map.MapDataCache
import org.junit.jupiter.api.BeforeEach

class MapDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = MapDataCache()
    }

}