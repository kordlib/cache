package dev.kord.cache.api

expect fun <K, V> concurrentHashMap(): MutableMap<K, V>
