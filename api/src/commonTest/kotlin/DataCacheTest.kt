import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.api.find
import dev.kord.cache.api.query
import kotlinx.coroutines.test.runTest
import kotlin.js.JsName
import kotlin.reflect.KType
import kotlin.test.Test

class DataCacheTest {

    private val nullCache = object : DataCache {
        override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? = null
        override suspend fun register(description: DataDescription<out Any, out Any>) {}
        override suspend fun register(descriptions: Iterable<DataDescription<out Any, out Any>>) {}
        override suspend fun register(vararg descriptions: DataDescription<out Any, out Any>) {}
    }

    private class NotRegistered(val id: String)

    @Suppress("DEPRECATION")
    @Test
    @JsName("test1")
    fun `nullCache doesn't throw on getting query`() = runTest {
        nullCache.query { NotRegistered::id eq "something" }.singleOrNull()
        nullCache.find { NotRegistered::id eq "something" }.singleOrNull()
    }
}
