@file:Suppress("EXPERIMENTAL_API_USAGE")

package caffeine

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.caffeine.CaffeineDataCache
import org.junit.jupiter.api.BeforeEach
import kotlin.test.BeforeTest

class CaffeineDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeTest
    fun setUp() {
        datacache = CaffeineDataCache()
    }


}