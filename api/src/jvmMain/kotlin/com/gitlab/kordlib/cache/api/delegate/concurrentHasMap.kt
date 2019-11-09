package com.gitlab.kordlib.cache.api.delegate

import java.util.concurrent.ConcurrentHashMap

actual inline fun<reified K, V> concurrentHashMap() : MutableMap<K,V> = ConcurrentHashMap()
