package com.gitlab.kordlib.cache.api.data

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * A description of the data to be stored in a cache.
 */
class DataDescription<T : Any, I>(
        val clazz: KClass<T>,
        val indexField: IndexField<T, I>,
        val links: List<DataLink<T, Any, Any?>> = listOf()
)

class DataLink<S : Any, T : Any, I>(val source: KProperty1<S, I>, val target: KClass<T>, val linkedField: KProperty1<T, I>)

inline fun <reified T : Any, I> description(index: KProperty1<T, I>, builder: LinkBuilder<T>.() -> Unit = {}
): DataDescription<T, I> {
    val clazz = T::class
    val field = IndexField(index)
    val links = LinkBuilder<T>().apply(builder).links
    return DataDescription(clazz, field, links)
}

class LinkBuilder<S : Any>(
        @PublishedApi
        internal val links: MutableList<DataLink<S, Any, Any?>> = mutableListOf()
) {

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any, I : Any?> link(pair: Pair<KProperty1<S, I>, KProperty1<T, I>>) {
        links.add(DataLink(pair.first, T::class as KClass<Any>, pair.second as KProperty1<Any, I>))
    }

}