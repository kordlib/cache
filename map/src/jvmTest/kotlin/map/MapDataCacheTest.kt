package map

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.map.MapDataCache
import kotlin.test.BeforeTest

class MapDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeTest
    fun setUp() {
        datacache = MapDataCache()
    }

}