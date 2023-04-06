package dev.kord.cache.api.observables

/**
 * A cache that associates a [Value] with an [Index].
 */
public interface EntryCache<Value : Any> {

    /**
     * Returns the [Value] associated with the specified [Index] key in this cache, or null if there is no
     * cached value for the given [Index].
     */
    public suspend fun get(key: Index): Value?

    /**
     * Returns the first [Value] that satisfies the given [transform] function, or null if none is found.
     */
    public suspend fun get(transform: (Value) -> Boolean): Value?

    /**
     * Associates the given [value] with a new [Index] in this cache. If the cache previously contained a
     * value associated with the same [Index], the old value is replaced by [value].
     * Returns the newly generated [Index] for [value].
     */
    public suspend fun put(value: Value): Index

    /**
     * Discards any cached [Value] that satisfies the given [transform] function.
     */
    public suspend fun discardIf(transform: (Value) -> Boolean)

    /**
     * Discards the cached [Value] associated with the given [Index], if it exists.
     * Returns the discarded [Value], or null if it was not found.
     */
    public suspend fun discard(index: Index): Value?

    /**
     * Discards all entries in the cache.
     */
    public suspend fun discardAll()

    /**
     * Adds an observer cache to this cache. Whenever a value is discarded from this cache, the
     * observer cache will also discard any values that are related to it.
     */
    public suspend fun <R: Any> relatesTo(other: EntryCache<R>, handler: RelationHandler<Value, R>)

    /**
     * Returns the [Relation] object for this cache, which contains information about any other
     * caches that are observing this one.
     */
    public suspend fun getRelations(): Relation<Value>

    /**
     * Returns a defensive copy of the cache entries as a [Map] of [Index] to [Value].
     */
    public suspend fun asMap(): Map<Index, Value>
}
