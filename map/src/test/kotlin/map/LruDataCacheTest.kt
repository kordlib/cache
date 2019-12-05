package map

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.data.description
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.api.put
import com.gitlab.kordlib.cache.api.putAll
import com.gitlab.kordlib.cache.map.MapDataCache
import com.gitlab.kordlib.cache.map.MapLikeCollection
import com.gitlab.kordlib.cache.map.lruLinkedHashMap
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class LRUEntry(val id: Int)

private val description = description(LRUEntry::id)

class LruDataCacheTest : DataCacheVerifier() {
    override lateinit var datacache: DataCache

    @BeforeEach
    fun setUp() {
        datacache = MapDataCache {
            forType<LRUEntry> { lruLinkedHashMap(4) }
            default { concurrentHashMap() }
        }
    }

    @Test
    fun `lru removes when size too big`() = runBlocking {
        val first = LRUEntry(0)
        val entries = IntRange(1, 4).map { LRUEntry(it) }

        datacache.register(description)
        datacache.put(first)
        datacache.putAll(entries)

        val actual = datacache.find<LRUEntry> { LRUEntry::id eq 0 }.singleOrNull()

        Assertions.assertEquals(null, actual)
    }

}