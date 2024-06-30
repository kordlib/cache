package dev.kord.cache.api.observables

/**
 * A typealias for a function that determines the relationship between two entities of type `T` and `R`
 * in a uni-directional link.
 * The `value` parameter represents the entity of type `T` and the `friend` parameter represents the entity of type `R`.
 * The function should return `true` if the two entities are related, or `false` otherwise.
 */
public typealias RelationHandler<T, R> = (value: T, friend: R) -> Boolean

/**
 * A `Relation` is a uni-directional link between two entities of type `T` and `R`.
 * A `Relation` is defined by a set of `RelationHandler`s which determine the relationship
 * between two entities.
 *
 * @param T the type of the first entity in the relation.
 */
public interface Relation<T: Any> {

    /**
     * Removes the given [value] from all caches related to this relation.
     *
     * @param value the entity to remove from the relation.
     */
    public suspend fun remove(value: T)


    /**
     * Associates an [Cache] of type `T` with this relation.
     *
     * @param cache the cache to associate with this relation.
     * @param handler the `RelationHandler` that defines the relationship between entities of type `T` and `R`.
     */
    public suspend fun <R: Any> to(cache: Cache<*, R>, handler: RelationHandler<T, R>)
}
