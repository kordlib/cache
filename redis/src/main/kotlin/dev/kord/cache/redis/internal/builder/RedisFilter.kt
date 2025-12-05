package dev.kord.cache.redis.internal.builder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import reactor.core.publisher.Flux


internal sealed class RedisFilter<T : Any, I> {

    abstract fun filterFlux(incoming: Flow<T>): Flow<T>

    abstract fun startFlux(): Flow<T>

}

internal sealed class IdentityFilter<T : Any, I> : RedisFilter<T, I>() {

    class SelectByIdFilter<T : Any, I>(
        private val info: QueryInfo<T, I>,
        private val identity: I
    ) : IdentityFilter<T, I>() {

        override fun startFlux(): Flow<T> = flow {
            val raw = info.commands.hget(info.entryName, info.keySerializer(identity)) ?: return@flow
            emit(info.binarySerializer.decodeFromByteArray(info.valueSerializer, raw))
        }

        override fun filterFlux(incoming: Flow<T>): Flow<T> = incoming
            .filter { info.description.indexField.property.get(it) == identity }


    }

    class SelectByIdsFilter<T : Any, I>(
        private val info: QueryInfo<T, I>,
        private val identities: List<I>
    ) : IdentityFilter<T, I>() {

        override fun startFlux(): Flow<T> {
            val keys = identities.map { info.keySerializer(it) }.toTypedArray()

            return info.commands.hmget(info.entryName, *keys)
                .map { it.value }
                .map { info.binarySerializer.decodeFromByteArray(info.valueSerializer, it) }
        }

        override fun filterFlux(incoming: Flow<T>): Flow<T> =
            incoming.filter { info.description.indexField.property.get(it) in identities }


    }
}

internal sealed class ValueFilter<T : Any, I> : RedisFilter<T, I>() {

    class PredicateFilter<T : Any, I>(
        private val info: QueryInfo<T, I>,
        private val predicate: (T) -> Boolean
    ) : ValueFilter<T, I>() {
        override fun filterFlux(incoming: Flow<T>): Flow<T> = incoming.filter(predicate)

        override fun startFlux(): Flow<T> = info.commands.hvals(info.entryName)
            .map { info.binarySerializer.decodeFromByteArray(info.valueSerializer, it) }
            .filter(predicate)
    }

}
