@file:Suppress("FunctionName", "EXPERIMENTAL_API_USAGE")

package com.gitlab.cord.tck

import com.gitlab.kord.cache.api.DataCache
import com.gitlab.kord.cache.api.data.description
import com.gitlab.kord.cache.api.find
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import kotlin.random.Random

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
        }
    }
}

private data class DataSource2(val id: Long, val source1Id: Long) {
    companion object {
        val description = description(DataSource2::id)
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

}