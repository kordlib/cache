package map

import com.gitlab.kordlib.cache.api.data.description
import com.gitlab.kordlib.cache.api.delegate.DelegatingDataCache
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.api.getEntry
import com.gitlab.kordlib.cache.map.MapLikeCollection
import com.gitlab.kordlib.cache.map.fromMapLike
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.KProperty1

private data class WeakEntity(val id: Int)

private val description = description(WeakEntity::id)

class WeakDataCacheTest {

    @Test
    @ExperimentalStdlibApi
    fun `map drops items for which there are no references to their keys`() = runBlocking {
        val dataCache = DelegatingDataCache.fromMapLike { MapLikeCollection.from(WeakHashMap<Any, Any>()) }

        dataCache.register(description)

        dataCache.getEntry<WeakEntity>()?.put(WeakEntity((500)))
        System.gc()

        val actual = dataCache.find<WeakEntity>().count()
        Assertions.assertEquals(0L, actual)
        Unit
    }

}
