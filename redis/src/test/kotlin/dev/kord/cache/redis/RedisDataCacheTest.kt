package dev.kord.cache.redis

import com.redis.testcontainers.RedisContainer
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.delegate.DelegatingDataCache
import dev.kord.cache.tck.TTLDataCacheVerifier
import io.lettuce.core.RedisClient
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class RedisDataCacheTest : TTLDataCacheVerifier() {
    companion object {
        @Container
        @JvmStatic
        private val redis = RedisContainer("redis:8")
    }

    @OptIn(InternalSerializationApi::class)
    override fun newCache(): DataCache {
        val configuration = RedisConfiguration {
            client = RedisClient.create(redis.redisURI)
        }

        return DelegatingDataCache {
            default { cache, description ->
                @Suppress("UNCHECKED_CAST")
                RedisEntryCache(
                    cache,
                    description,
                    configuration,
                    serializers.get<Any, Any>(description.type) as? KSerializer<Any> ?: description.klass.serializer()
                )
            }
        }
    }


}
