@file:JvmName("MapLikeCollectionJvmx")
package dev.kord.cache.map

import java.util.*

/**
 * Wraps a [WeakHashMap] into a [MapLikeCollection].
 */
fun <KEY, VALUE : Any> MapLikeCollection.Companion.weakHashMap(): MapLikeCollection<KEY, VALUE> =
    from(WeakHashMap())
