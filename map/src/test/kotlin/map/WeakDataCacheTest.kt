package map

import dev.kord.cache.api.data.description
import dev.kord.cache.api.find
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
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

        val actual = dataCache.query<WeakEntity>().count()
        Assertions.assertEquals(0L, actual)
        Unit
    }

}
