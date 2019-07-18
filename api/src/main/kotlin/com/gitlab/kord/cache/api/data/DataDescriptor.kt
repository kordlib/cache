package com.gitlab.kord.cache.api.data

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * A description of the data to be stored in a cache.
 */
class DataDescriptor<T : Any, I> (val clazz: KClass<T>, val indexField: IndexField<T, I>)

