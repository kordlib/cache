package dev.kord.cache.redis

import dev.kord.cache.api.DataCache
import dev.kord.cache.api.delegate.DelegatingDataCache
import dev.kord.cache.tck.DataCacheVerifier
import io.lettuce.core.RedisClient
import kotlinx.serialization.KSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import redis.embedded.RedisServer
import redis.embedded.RedisServerBuilder
import kotlin.concurrent.thread

class RedisDataCacheTest : DataCacheVerifier() {

    override lateinit var datacache: DataCache

    lateinit var configuration: RedisConfiguration

    companion object {
        lateinit var server: RedisServer

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            println("starting server...")
            server = RedisServerBuilder()
                    .port(6379)
                    .setting("maxheap 128M")
                    .build()

            server.start()
            Runtime.getRuntime().addShutdownHook(thread(false) {
                server.stop()
            })
        }

    }

    @ExperimentalStdlibApi
    @BeforeEach
    fun setUp() {
        configuration = RedisConfiguration()
        datacache = DelegatingDataCache {
            default { cache, description ->
                RedisEntryCache(cache, description, configuration, serializers.get<Any,Any>(description.type) as KSerializer<Any>)
            }
        }
    }

    @AfterEach
    fun tearDown() {
        configuration.client.shutdown()
    }

}