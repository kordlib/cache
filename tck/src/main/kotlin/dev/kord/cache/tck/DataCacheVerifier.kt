@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package dev.kord.cache.tck

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.DataDescription
import dev.kord.cache.api.data.description
import dev.kord.cache.api.find
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.reflect.typeOf

@Serializable
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

@Serializable
private data class DataSource2(val id: Long, val source1Id: Long) {
    companion object {
        val description = description(DataSource2::id)
    }
}

@Serializable
private data class OptionalDataSource(val id: Long, val source1Id: Long? = null) {
    companion object {
        val description = description(OptionalDataSource::id)
    }
}

private val thread1 = newSingleThreadContext("dev.kord.tck.test thread1")

abstract class DataCacheVerifier {

    val serializers: Map<DataDescription<*, *>, KSerializer<*>> = mapOf(
            DataSource1.description to DataSource1.serializer(),
            DataSource2.description to DataSource2.serializer(),
            OptionalDataSource.description to OptionalDataSource.serializer()
    )

    abstract var datacache: DataCache

    @Test
    fun `concurrent reading and writing should be allowed`() = runBlocking {
        datacache.register(DataSource1.description, DataSource2.description)

        generateSequence { DataSource1.random() }.take(10).forEach { datacache.put(it) }

        val deferred = async(thread1) {
            datacache.query<DataSource1>().asFlow().onEach {
                delay(10)
            }.launchIn(this)
        }

        delay(10)
        datacache.query<DataSource1>().remove()

        deferred.await()

        Unit
    }

    @RepeatedTest(10)
    fun `delete should cascade`(): Unit = runBlocking {
        val one = DataSource1.random()
        val two = DataSource2(Random.nextLong(), one.id)

        datacache.register(DataSource1.description, DataSource2.description)

        datacache.put(one)
        datacache.put(two)

        datacache.query<DataSource1> {
            DataSource1::id eq one.id
        }.remove()

        val actualTwo = datacache.query<DataSource2> {
            DataSource2::id eq two.id
        }.singleOrNull()

        Assertions.assertEquals(null, actualTwo)
    }

    @RepeatedTest(10)
    fun `insert should be queryable`(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        val actualOne = datacache.query<DataSource1> { DataSource1::id eq one.id }.single()

        Assertions.assertEquals(one, actualOne)
    }

    @RepeatedTest(10)
    fun `removed entry should not be found`(): Unit = runBlocking {
        val one = DataSource1.random()

        val others = generateSequence { DataSource1.random() }
                .take(Random.nextInt(100))
                .toList() + one

        datacache.register(DataSource1.description)

        others.shuffled().forEach {
            datacache.put(it)
        }

        datacache.query<DataSource1> { DataSource1::id eq one.id }.remove()

        val actualOne = datacache.query<DataSource1> { DataSource1::id eq one.id }.singleOrNull()

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

        datacache.query<DataSource1> { DataSource1::id eq one.id }.remove()
    }

    @Test
    fun `update that changes identity throws`(): Unit = runBlocking {
        val one = DataSource1.random()
        datacache.register(DataSource1.description)
        datacache.put(one)

        assertThrows<Exception> {
            runBlocking {
                datacache.query<DataSource1> { DataSource1::id eq one.id }.update {
                    it.copy(id = it.id + 1)
                }
            }
        }

        Unit
    }

    @RepeatedTest(10)
    fun `delete should not cascade nullable`(): Unit = runBlocking {
        val one = DataSource1.random()
        val optional = OptionalDataSource(Random.nextLong(), null)

        datacache.register(DataSource1.description, OptionalDataSource.description)

        datacache.put(one)
        datacache.put(optional)

        datacache.query<DataSource1>().remove()

        val actualOptional = datacache.query<OptionalDataSource> {
            OptionalDataSource::id eq optional.id
        }.singleOrNull()

        Assertions.assertEquals(optional, actualOptional)
    }

    @RepeatedTest(10)
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

        datacache.query<DataSource1> { DataSource1::id eq one.id }.update {
            it.copy(fieldOne = "something random").also { updated = it }
        }

        val actualOne = datacache.query<DataSource1> { DataSource1::id eq one.id }.singleOrNull()

        Assertions.assertEquals(updated, actualOne)
    }

}
