package map

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.map.MapDataCache
import org.junit.jupiter.api.BeforeEach

class MapDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = MapDataCache()
    }

}