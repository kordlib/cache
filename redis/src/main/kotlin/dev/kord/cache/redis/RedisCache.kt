package dev.kord.cache.redis

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.data.DataDescription
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KType



class RedisCache(
        private val configuration: RedisConfiguration,
        private val serializers: Map<DataDescription<*,*>, KSerializer<*>>
) : DataCache {
    private val registered = mutableMapOf<KType, RedisEntryCache<*,*>>()


    @Suppress("UNCHECKED_CAST")
    @OptIn(ImplicitReflectionSerializer::class)
    override suspend fun register(description: DataDescription<out Any, out Any>) {
        val castDescription = description as DataDescription<Any, Any>
        val serializer = serializers[castDescription] as? KSerializer<Any> ?: description.klass.serializer()
        registered[castDescription.type] = RedisEntryCache(this, castDescription, configuration, serializer = serializer)
    }

    override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? = registered[type] as? DataEntryCache<T>

}