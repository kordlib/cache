package dev.kord.cache.api

import java.util.concurrent.ConcurrentHashMap

actual class ConcurrentHashMap<K, V> : MutableMap<K, V> by ConcurrentHashMap()
