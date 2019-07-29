package map

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.data.description
import com.gitlab.kord.cache.api.find
import com.gitlab.kord.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class MapDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = MapDataCache()
    }

}