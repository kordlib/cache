@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package com.gitlab.cord.tck

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.data.description
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.api.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random
import kotlin.test.Asserter
import kotlin.test.DefaultAsserter
import kotlin.test.Test
import kotlin.test.assertFails

expect fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T

private data class DataSource1(val id: Long, val fieldOne: String, val fieldTwo: Boolean) {
    companion object {
        fun random(): DataSource1 {
            val id = Random.nextLong()
            val fieldOne = generateSequence { Random.nextInt().toString() }.take(Random.nextInt(100)).joinToString()
            val fieldTwo = Random.nextBoolean()

            return DataSource1(id, fieldOne, fieldTwo)
        }

        val description = description(DataSource1::id) {
            link(DataSource1::id to DataSource2::source1Id)
            link(DataSource1::id to OptionalDataSource::source1Id)
        }
    }
}

private data class DataSource2(val id: Long, val source1Id: Long) {
    companion object {
        val description = description(DataSource2::id)
    }
}

private data class OptionalDataSource(val id: Long, val source1Id: Long?) {
    companion object {
        val description = description(OptionalDataSource::id)
    }
}

abstract class DataCacheVerifier {

    abstract var datacache: DataCache
    var asserter: Asserter = DefaultAsserter
    var timeoutMs: Long = 10_000

    @Test
    fun deleteShouldCascade(): Unit = runBlocking {
        val one = DataSource1.random()
        val two = DataSource2(Random.nextLong(), one.id)

        datacache.register(DataSource1.description, DataSource2.description)

        datacache.put(one)
        datacache.put(two)

        datacache.find<DataSource1> {
            DataSource1::id eq one.id
        }.remove()

        val actualTwo = datacache.find<DataSource2> {
            DataSource2::id eq two.id
        }.singleOrNull()

        asserter.assertNull("Expected linked entry to be removed upon removal of primary entry", actualTwo)
    }

    @Test
    fun insertShouldBeQueryable(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        val actualOne = datacache.find<DataSource1> { DataSource1::id eq one.id }.single()

        asserter.assertEquals("Expected inserted entry to be present on index query", one, actualOne)
    }

    @Test
    fun removedEntryShouldNotBePresent(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        datacache.find<DataSource1> { DataSource1::id eq one.id }.remove()

        val actualOne = datacache.find<DataSource1> { DataSource1::id eq one.id }.singleOrNull()

        asserter.assertNull("Expected removed entry to no longer be present on index query", actualOne)
    }

    @Test
    fun cyclicalRemoveShouldNotCauseLoop(): Unit = runBlocking {
        val one = DataSource1.random()
        val two = DataSource2(Random.nextLong(), one.id)

        datacache.register(DataSource1.description, description(DataSource2::id) {
            link(DataSource2::source1Id to DataSource1::id)
        })

        datacache.put(one)
        datacache.put(two)

        withTimeout(timeoutMs) {
            datacache.find<DataSource1> { DataSource1::id eq one.id }.remove()
        }
    }

    @Test
    fun UpdateThatChangesIdentityShouldThrow(): Unit = runBlocking {
        val one = DataSource1.random()
        datacache.register(DataSource1.description)
        datacache.put(one)

        assertFails {
            datacache.find<DataSource1> { DataSource1::id eq one.id }.update {
                it.copy(id = it.id + 1)
            }
        }
        Unit
    }

    @Test
    fun deleteShouldNotCascadeUnlinkedNullables(): Unit = runBlocking {
        val one = DataSource1.random()
        val optional = OptionalDataSource(Random.nextLong(), null)

        datacache.register(DataSource1.description, OptionalDataSource.description)

        datacache.put(one)
        datacache.put(optional)

        datacache.find<DataSource1> {
            DataSource1::id eq one.id
        }.remove()

        datacache.find<DataSource1>().remove()

        val actualOptional = datacache.find<OptionalDataSource> {
            OptionalDataSource::id eq optional.id
        }.singleOrNull()

        asserter.assertEquals(null, optional, actualOptional)
    }

    @Test
    fun updateShouldModifyCorrectly(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        lateinit var updated: DataSource1

        datacache.find<DataSource1> { DataSource1::id eq one.id }.update {
            it.copy(fieldOne = "something random").also { updated = it }
        }

        val actualOne = datacache.find<DataSource1> { DataSource1::id eq one.id }.singleOrNull()

        asserter.assertEquals(null, updated, actualOne)
    }

}
