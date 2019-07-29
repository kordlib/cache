package caffeine

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.caffeine.CaffeineDataCache
import org.junit.jupiter.api.BeforeEach

class CaffeineDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = CaffeineDataCache()
    }


}