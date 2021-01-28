package dev.kord.benchmarks

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.map.MapDataCache
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 3, warmups = 3)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class Insertions {

    lateinit var cache: DataCache
    lateinit var map: ConcurrentHashMap<Long, Data>
    lateinit var random: Random

    @Setup(Level.Iteration)
    fun setup() = runBlocking {
        cache = MapDataCache()
        cache.register(Data.description)
        random = Random(1337)
        map = ConcurrentHashMap()
    }

    @Benchmark
    fun mapCacheInsert() = runBlocking {
        cache.put(Data(random.nextLong()))
    }

    @Benchmark
    fun mapDirectInsert() = runBlocking {
        val data = Data(random.nextLong())
        map[data.id] = data
    }

    class Data(val id: Long) {
        companion object {
            val description = description(Data::id)
        }
    }

}