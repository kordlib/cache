package dev.kord.cache.redis

import com.gitlab.cord.tck.DataCacheVerifier
import com.gitlab.kordlib.cache.api.DataCache
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import redis.embedded.RedisServer
import redis.embedded.RedisServerBuilder

class RedisDataCacheTest : DataCacheVerifier() {

    override lateinit var datacache: DataCache

    var configuration = RedisConfiguration()

    val server = RedisServerBuilder()
            .port(6379)
            .setting("maxheap 128M")
            .build()

    @ExperimentalStdlibApi
    @BeforeEach
    fun setUp() {
        server.start()
        datacache = RedisCache(configuration, serializers)
    }

    @AfterEach
    fun tearDown() {
        server.stop()
    }

}