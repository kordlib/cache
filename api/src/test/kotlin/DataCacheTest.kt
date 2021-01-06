import dev.kord.cache.api.DataCache
import dev.kord.cache.api.DataEntryCache
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.api.find
import dev.kord.cache.api.query
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.reflect.KType

class DataCacheTest {

    private val nullCache = object : DataCache {
        override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? = null
        override suspend fun register(description: DataDescription<out Any, out Any>) {}
        override suspend fun register(descriptions: Iterable<DataDescription<out Any, out Any>>) {}
        override suspend fun register(vararg descriptions: DataDescription<out Any, out Any>) {}
    }

    private class NotRegistered(val id: String)

    @Test
    fun `nullCache doesn't throw on getting query`(): Unit = runBlocking {
        nullCache.query<NotRegistered> { NotRegistered::id eq "something" }.singleOrNull()
        nullCache.find<NotRegistered> { NotRegistered::id eq "something" }.singleOrNull()

        Unit
    }


}