package dev.kord.benchmarks

import co.touchlab.stately.collections.ConcurrentMutableMap
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
import kotlinx.benchmark.*
import kotlinx.coroutines.test.runTest
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class Queries {

    private lateinit var cache: DataCache
    private lateinit var map: MutableMap<Long, Data>
    private val seed = 1337
    private val random = Random(seed)

    @Setup
    fun setup() = runTest {
        cache = MapDataCache()
        map = ConcurrentMutableMap()

        cache.register(Data.description)
        val random = Random(seed)

        generateSequence {
            random.nextLong()
        }.take(1_000_000).forEach {
            cache.put(Data(it))
            map[it] = Data(it)
        }

    }

    @Benchmark
    fun mapCacheQuery(blackhole: Blackhole) = runTest {
        val item = cache.query { Data::id eq random.nextLong() }.singleOrNull()
        blackhole.consume(item)
    }

    @Benchmark
    fun mapDirectQuery(blackhole: Blackhole) = runTest {
        val item = map[random.nextLong()]
        blackhole.consume(item)
    }

    class Data(val id: Long) {
        companion object {
            val description = description(Data::id)
        }
    }
}
