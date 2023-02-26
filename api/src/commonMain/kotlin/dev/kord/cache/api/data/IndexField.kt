package dev.kord.cache.api.data

import kotlin.reflect.KProperty1

/**
 * The field on which a Cache can index values, this [property] is assumed to be unique for different entries.
 */
class IndexField<T, R>(val property: KProperty1<T, R>)
