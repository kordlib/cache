package dev.kord.cache.api.observables

class BasicRelation<T: Any> : Relation<T> {
    private val relations = mutableSetOf<RelationHandler<T, Any>>()
    private val caches = mutableSetOf<Cache<*, *>>()

    override suspend fun remove(value: T) {
        relations.onEach { relatesTo ->
            caches.onEach { other -> other.removeAny { friend -> relatesTo(value, friend) } }
        }
    }

    override suspend fun <R : Any> to(cache: Cache<*, R>, handler: RelationHandler<T, R>) {
        caches.add(cache)
        relations.add(safe(handler))
    }

    private fun <R: Any> safe(relationHandler: RelationHandler<T, R>): RelationHandler<T, Any> {
        return obj@{ value: T, friend: Any ->
            @Suppress("UNCHECKED_CAST")
            val safeCast = friend as? R
            safeCast != null && relationHandler(value, safeCast)
        }
    }
}