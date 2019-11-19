package com.gitlab.kordlib.cache.map

import java.util.*

fun <KEY, VALUE : Any> MapLikeCollection.Companion.weakHashMap(): MapLikeCollection<KEY, VALUE> =
        from(WeakHashMap())
