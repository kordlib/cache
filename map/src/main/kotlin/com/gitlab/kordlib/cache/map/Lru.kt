package com.gitlab.kordlib.cache.map

/**
 * Creates a least recently used map using a [LinkedHashMap]. The map will start evicting entries once [maxSize] has been reached.
 */
fun <KEY, VALUE : Any> MapLikeCollection.Companion.lruLinkedHashMap(maxSize: Int): MapLikeCollection<KEY, VALUE> =
        caffeine(LRULinkedHashMap(maxSize))

internal class LRULinkedHashMap<K, V>(
        private val maxSize: Int
) : LinkedHashMap<K, V>(maxSize, 1.0F, true), MutableMap<K, V> {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > maxSize

}