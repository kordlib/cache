@file:OptIn(CacheExperimental::class)

package dev.kord.cache.tck

import dev.kord.cache.api.annotation.CacheExperimental
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

abstract class TTLDataCacheVerifier : DataCacheVerifier() {
    @Test
    @RepeatedTest(10, SHORT_DISPLAY_NAME)
    @JsName("test8")
    fun `test value correctly expires`() = runTestWithDataCache {
        val one = DataSource1.random()
        datacache.register(DataSource1.description)
        datacache.put(one, 1.seconds)

        val value = datacache.query { DataSource1::id eq one.id }.singleOrNull()

        assertEquals(one, value)

        // TestScheduler skips delay calls, so we need to exit test scheduler
        withContext(Dispatchers.Default) {
            delay(1.seconds)
        }
        val value2 = datacache.query { DataSource1::id eq one.id }.singleOrNull()
        assertNull(value2)
    }

}
