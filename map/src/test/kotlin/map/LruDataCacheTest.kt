package map

import dev.kord.cache.tck.DataCacheVerifier
import dev.kord.cache.api.*
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.putAll
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
import dev.kord.cache.map.MapLikeCollection
import dev.kord.cache.map.lruLinkedHashMap
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

        val actual = datacache.query<LRUEntry> { LRUEntry::id eq 0 }.singleOrNull()

        Assertions.assertEquals(null, actual)
    }

}