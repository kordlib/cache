package dev.kord.benchmarks

import co.touchlab.stately.collections.ConcurrentMutableMap
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.map.MapDataCache
import kotlinx.benchmark.*
import kotlinx.coroutines.test.runTest
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 5, timeUnit = BenchmarkTimeUnit.SECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class Insertions {

    lateinit var cache: DataCache
    lateinit var map: ConcurrentMutableMap<Long, Data>
    lateinit var random: Random

    @Setup
    fun setup() = runTest {
        cache = MapDataCache()
        cache.register(Data.description)
        random = Random(1337)
        map = ConcurrentMutableMap()
    }

    @Benchmark
    fun mapCacheInsert() = runTest {
        cache.put(Data(random.nextLong()))
    }

    @Benchmark
    fun mapDirectInsert() = runTest {
        val data = Data(random.nextLong())
        map[data.id] = data
    }

    class Data(val id: Long) {
        companion object {
            val description = description(Data::id)
        }
    }
}
