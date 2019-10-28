@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package com.gitlab.cord.tck

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.data.description
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.api.getEntry
import com.gitlab.kordlib.cache.api.put
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.reflect.typeOf

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

    @RepeatedTest(50)
    fun `delete should cascade`(): Unit = runBlocking {
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

        Assertions.assertEquals(null, actualTwo)
    }

    @RepeatedTest(50)
    fun `insert should be queryable`(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        val actualOne = datacache.find<DataSource1> { DataSource1::id eq one.id }.single()

        Assertions.assertEquals(one, actualOne)
    }

    @RepeatedTest(50)
    fun `removed entry should not be found`(): Unit = runBlocking {
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

        Assertions.assertEquals(null, actualOne)
    }

    @RepeatedTest(1)
    fun `cyclical remove should not hang`(): Unit = runBlocking {
        val one = DataSource1.random()
        val two = DataSource2(Random.nextLong(), one.id)

        datacache.register(DataSource1.description, description(DataSource2::id) {
            link(DataSource2::source1Id to DataSource1::id)
        })

        datacache.put(one)
        datacache.put(two)

        datacache.find<DataSource1> { DataSource1::id eq one.id }.remove()
    }

    @Test
    fun `update that changes identity throws`(): Unit = runBlocking {
        val one = DataSource1.random()
        datacache.register(DataSource1.description)
        datacache.put(one)

        assertThrows<Exception> {
            runBlocking {
                datacache.find<DataSource1> { DataSource1::id eq one.id }.update {
                    it.copy(id = it.id + 1)
                }
            }
        }

        Unit
    }

    @RepeatedTest(50)
    fun `delete should not cascade nullable`(): Unit = runBlocking {
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

        Assertions.assertEquals(optional, actualOptional)
    }

    @RepeatedTest(50)
    fun `update should modify correctly`(): Unit = runBlocking {
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

        Assertions.assertEquals(updated, actualOne)
    }

}
