package dev.kord.cache.api.observables

class BasicRelation<T: Any> : Relation<T> {
    private val relations = mutableSetOf<RelationHandler<T, Any>>()
    private val caches = mutableSetOf<EntryCache<*>>()

    override suspend fun remove(value: T) {
        relations.onEach { relatesTo ->
            caches.onEach { other -> other.removeIf { friend -> relatesTo(value, friend) } }
        }
    }

    override suspend fun <R : Any> to(cache: EntryCache<R>, handler: RelationHandler<T, R>) {
        caches.add(cache as EntryCache<*>)
        relations.add(safe(handler))
    }

    private fun <R: Any> safe(relationHandler: RelationHandler<T, R>): RelationHandler<T, Any> {
        return obj@{ value: T, friend: Any ->
            @Suppress("UNCHECKED_CAST")
            val safeCast = friend as? R
            if(safeCast != null) return@obj relationHandler(value, safeCast)
            else return@obj false
        }
    }
}