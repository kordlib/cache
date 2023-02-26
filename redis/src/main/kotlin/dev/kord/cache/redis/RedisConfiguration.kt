package dev.kord.cache.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.protobuf.ProtoBuf

class RedisConfiguration(
        val binaryFormat: BinaryFormat,
        val client: RedisClient,
        val prefix: String,
        val reuseConnection: Boolean,
        val codec: RedisCodec<ByteArray, ByteArray>
) {
    private val reusedConnection: StatefulRedisConnection<ByteArray, ByteArray> by lazy {
        client.connect(codec)
    }

    val connection: StatefulRedisConnection<ByteArray, ByteArray>
        get() = if (reuseConnection) reusedConnection
        else client.connect(codec)

    companion object {
        inline operator fun invoke(builder: Builder.() -> Unit = {}): RedisConfiguration = Builder().apply(builder).build()
    }


    object Defaults {
        const val DEFAULT_KEY_PREFIX = "dev:kord:cache:"
        const val DEFAULT_URL = "redis://localhost"

        const val KORD_REDIS_URL = "KORD_REDIS_URL"

        val binaryFormat: BinaryFormat = ProtoBuf {
            encodeDefaults = false
        }
        val codec: RedisCodec<ByteArray, ByteArray> = ByteArrayCodec.INSTANCE
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder {
        var keyPrefix: String = Defaults.DEFAULT_KEY_PREFIX
        var url: String? = null

        var client: RedisClient? = null

        var binaryFormat: BinaryFormat = Defaults.binaryFormat
        var codec: RedisCodec<ByteArray, ByteArray> = Defaults.codec

        var reuseConnection: Boolean = true

        private fun url() = url ?: System.getenv(Defaults.KORD_REDIS_URL) ?: Defaults.DEFAULT_URL

        fun client(): RedisClient = RedisClient.create(url())

        fun build(): RedisConfiguration = RedisConfiguration(
                binaryFormat = binaryFormat,
                client = client ?: client(),
                prefix = keyPrefix,
                reuseConnection = reuseConnection,
                codec = codec
        )

    }


}
