package dev.kord.cache.api

import co.touchlab.stately.collections.ConcurrentMutableMap

actual fun <K, V> concurrentHashMap(): MutableMap<K, V> = ConcurrentMutableMap()
