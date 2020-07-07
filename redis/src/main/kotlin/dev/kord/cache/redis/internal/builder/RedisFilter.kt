package dev.kord.cache.redis.internal.builder

import reactor.core.publisher.Flux


internal sealed class RedisFilter<T : Any, I> {

    abstract fun filterFlux(incoming: Flux<T>): Flux<T>

    abstract fun startFlux(): Flux<T>

}

internal sealed class IdentityFilter<T : Any, I> : RedisFilter<T, I>() {

    class SelectByIdFilter<T : Any, I>(
            private val info: QueryInfo<T, I>,
            private val identity: I
    ) : IdentityFilter<T, I>() {

        override fun startFlux(): Flux<T> {
            return info.commands.hget(info.entryName, info.keySerializer(identity))
                    .flux()
                    .map { info.binarySerializer.load(info.valueSerializer, it) }
        }

        override fun filterFlux(incoming: Flux<T>): Flux<T> = incoming
                .filter { info.description.indexField.property.get(it) == identity }


    }

    class SelectByIdsFilter<T : Any, I>(
            private val info: QueryInfo<T, I>,
            private val identities: List<I>
    ) : IdentityFilter<T, I>() {

        override fun startFlux(): Flux<T> {
            val keys = identities.map { info.keySerializer(it) }.toTypedArray()

            return info.commands.hmget(info.entryName, *keys)
                    .map { it.value }
                    .map { info.binarySerializer.load(info.valueSerializer, it) }
        }

        override fun filterFlux(incoming: Flux<T>): Flux<T> =
                incoming.filter { info.description.indexField.property.get(it) in identities }


    }
}

internal sealed class ValueFilter<T : Any, I> : RedisFilter<T, I>() {

    class PredicateFilter<T : Any, I>(
            private val info: QueryInfo<T, I>,
            private val predicate: (T) -> Boolean
    ) : ValueFilter<T, I>() {
        override fun filterFlux(incoming: Flux<T>): Flux<T> = incoming.filter(predicate)

        override fun startFlux(): Flux<T> = info.commands.hvals(info.entryName)
                .map { info.binarySerializer.load(info.valueSerializer, it) }
                .filter(predicate)
    }

}
