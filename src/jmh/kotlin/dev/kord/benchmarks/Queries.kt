package dev.kord.benchmarks

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 3, warmups = 3)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Queries {

    private lateinit var cache: DataCache
    private lateinit var map: MutableMap<Long, Data>
    private val seed = 1337
    private val random = Random(seed)

    @Setup(Level.Iteration)
    fun setup() = runBlocking {
        cache = MapDataCache()
        map = ConcurrentHashMap()

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
    fun mapCacheQuery(blackhole: Blackhole) = runBlocking {
        val item = cache.query<Data> { Data::id eq random.nextLong() }.singleOrNull()
        blackhole.consume(item)
    }

    @Benchmark
    fun mapDirectQuery(blackhole: Blackhole) = runBlocking {
        val item = map[random.nextLong()]
        blackhole.consume(item)
    }

    class Data(val id: Long) {
        companion object {
            val description = description(Data::id)
        }
    }

}