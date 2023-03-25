package map

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.putAll
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.map.lruLinkedHashMap
import dev.kord.cache.tck.DataCacheVerifier
import kotlin.test.Test
import kotlin.test.assertNull

private class LRUEntry(val id: Int)

private val description = description(LRUEntry::id)

class LruDataCacheTest : DataCacheVerifier() {
    override fun newCache(): DataCache {
        return MapDataCache {
            forType<LRUEntry> { lruLinkedHashMap(4) }
            default { concurrentHashMap() }
        }
    }

    @Test
    fun `lru removes when size too big`() = runTestWithDataCache {
        val first = LRUEntry(0)
        val entries = IntRange(1, 4).map { LRUEntry(it) }

        datacache.register(description)
        datacache.put(first)
        datacache.putAll(entries)

        val actual = datacache.query { LRUEntry::id eq 0 }.singleOrNull()

        assertNull(actual)
    }

}
