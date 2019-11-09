package com.gitlab.kordlib.cache.api.delegate

actual inline fun <reified K, V> concurrentHashMap(): MutableMap<K, V> = mutableMapOf()