package map

import com.gitlab.kordlib.cache.api.data.description
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.api.put
import com.gitlab.kordlib.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private data class WeakEntity(val id: Int)

private val description = description(WeakEntity::id)

class WeakDataCacheTest {

    @Test
    @ExperimentalStdlibApi
    fun `map drops items for which there are no references to their keys`() = runBlocking {
        val dataCache = MapDataCache {
            forType<WeakEntity> { weakHashMap() }
        }

        dataCache.register(description)

        dataCache.put(WeakEntity(500))
        System.gc()

        val actual = dataCache.find<WeakEntity>().count()
        Assertions.assertEquals(0L, actual)
        Unit
    }

}
