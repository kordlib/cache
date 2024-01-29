package dev.kord.cache.api

import java.util.concurrent.ConcurrentHashMap

actual fun <K, V> concurrentHashMap(): MutableMap<K, V> = ConcurrentHashMap<K, V>()
