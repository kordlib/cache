package map

import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.map.weakHashMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private data class WeakEntity(val id: Int)

private val description = description(WeakEntity::id)

class WeakDataCacheTest {

    @Test
    fun `map drops items for which there are no references to their keys`() = runTest {
        val dataCache = MapDataCache {
            forType<WeakEntity> { weakHashMap() }
        }

        dataCache.register(description)

        dataCache.put(WeakEntity(500))
        System.gc()

        val actual = dataCache.query<WeakEntity>().count()
        assertEquals(0L, actual)
    }

}
