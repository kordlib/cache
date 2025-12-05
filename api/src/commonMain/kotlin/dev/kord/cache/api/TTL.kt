package dev.kord.cache.api

import dev.kord.cache.api.annotation.CacheExperimental
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Extension of [DataEntryCache] which supports TTLs.
 */
interface DataEntryCacheWithTTL<VALUE : Any> : DataEntryCache<VALUE> {

    @OptIn(CacheExperimental::class)
    override suspend fun put(item: VALUE) = put(item, ttl = null)
    /**
     * Inserts a new expiring [item] into the cache for [ttl]. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     *
     * @param ttl the [Duration] after which the entry should expire or `null` for permanent items
     */
    @CacheExperimental
    suspend fun put(item: VALUE, ttl: Duration?)

    /**
     * Inserts a new [item] into the cache [until] it expires. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     *
     * @param clock the [Clock] to use to look up the current time
     * @throws IllegalArgumentException if [until] is in the past
     */
    @CacheExperimental
    suspend fun put(item: VALUE, until: Instant, clock: Clock = Clock.System) {
        val now = clock.now()
        check(now > until) { "Until parameter must be in the future" }
        return put(item, until - now)
    }

    /**
     * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    @CacheExperimental
    suspend fun put(vararg items: VALUE, ttl: Duration) = items.forEach { put(it, ttl) }

    /**
     * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    @CacheExperimental
    suspend fun put(items: Iterable<VALUE>, ttl: Duration) = items.forEach { put(it, ttl) }

    /**
     * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     */
    @CacheExperimental
    suspend fun put(items: Flow<VALUE>, ttl: Duration) = items.collect { put(it, ttl) }

    /**
     * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     *
     * @param clock the [Clock] to use to look up the current time
     * @throws IllegalArgumentException if [until] is in the past
     */
    @CacheExperimental
    suspend fun put(vararg items: VALUE, until: Instant, clock: Clock = Clock.System) =
        items.forEach { put(it, until, clock) }

    /**
     * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     *
     * @param clock the [Clock] to use to look up the current time
     * @throws IllegalArgumentException if [until] is in the past
     */
    @CacheExperimental
    suspend fun put(items: Iterable<VALUE>, until: Instant, clock: Clock = Clock.System) =
        items.forEach { put(it, until, clock) }

    /**
     * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
     * is already present will cause the old value to be overwritten.
     *
     * @param clock the [Clock] to use to look up the current time
     * @throws IllegalArgumentException if [until] is in the past
     */
    @CacheExperimental
    suspend fun put(items: Flow<VALUE>, until: Instant, clock: Clock = Clock.System) =
        items.collect { put(it, until, clock) }
}

@PublishedApi
internal inline fun <reified VALUE : Any> DataCache.getTTLEntry(): DataEntryCacheWithTTL<VALUE>? {
    val entry = getEntry<VALUE>() ?: return null
    return entry as? DataEntryCacheWithTTL<VALUE> ?: error("${entry::class.simpleName} does not support TTL")
}

/**
 * Inserts a new expiring [item] into the cache for [ttl]. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
@CacheExperimental
suspend inline fun <reified VALUE : Any> DataCache.put(item: VALUE, ttl: Duration) =
    getTTLEntry<VALUE>()?.put(item, ttl)

/**
 * Inserts a new [item] into the cache [until] it expires. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 *
 * @param clock the [Clock] to use to look up the current time
 * @throws IllegalArgumentException if [until] is in the past
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(item: VALUE, until: Instant, clock: Clock = Clock.System) =
    getTTLEntry<VALUE>()?.put(item, until, clock)

/**
 * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(vararg items: VALUE, ttl: Duration) =
    getTTLEntry<VALUE>()?.put(items.asIterable(), ttl)

/**
 * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(items: Iterable<VALUE>, ttl: Duration) =
    getTTLEntry<VALUE>()?.put(items, ttl)

/**
 * Inserts new expiring [items] into the cache for [ttl]. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(items: Flow<VALUE>, ttl: Duration) =
    getTTLEntry<VALUE>()?.put(items, ttl)

/**
 * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 *
 * @param clock the [Clock] to use to look up the current time
 * @throws IllegalArgumentException if [until] is in the past
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(
    vararg items: VALUE,
    until: Instant,
    clock: Clock = Clock.System
) = getTTLEntry<VALUE>()?.put(items.asIterable(), until, clock)

/**
 * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 *
 * @param clock the [Clock] to use to look up the current time
 * @throws IllegalArgumentException if [until] is in the past
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(
    items: Iterable<VALUE>,
    until: Instant,
    clock: Clock = Clock.System
) = getTTLEntry<VALUE>()?.put(items, until, clock)

/**
 * Inserts new [items] into the cache [until] it expires. Inserting an entry with an id that
 * is already present will cause the old value to be overwritten.
 *
 * @param clock the [Clock] to use to look up the current time
 * @throws IllegalArgumentException if [until] is in the past
 */
@OptIn(CacheExperimental::class)
suspend inline fun <reified VALUE : Any> DataCache.put(
    items: Flow<VALUE>,
    until: Instant,
    clock: Clock = Clock.System
) = getTTLEntry<VALUE>()?.put(items, until, clock)
