package com.gitlab.kordlib.cache.api.data

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A description of the data to be stored in a cache.
 */
class DataDescription<T : Any, I>(
        val type: KType,
        val klass: KClass<T>,
        val indexField: IndexField<T, I>,
        val links: List<DataLink<T, Any, Any?>> = listOf()
)

/**
 * An instance that links a Property [I] of class [S] to class [T]. When [S] is removed from the cache all [T] with an [I] pof the same
 * value will also be removed.
 */
class DataLink<S : Any, T : Any, I>(val source: KProperty1<S, I>, val target: KType, val linkedField: KProperty1<T, I>)

inline fun <reified T : Any, I> description(
        index: KProperty1<T, I>,
        builder: LinkBuilder<T>.() -> Unit = {}
): DataDescription<T, I> {
    val type = typeOf<T>()
    val field = IndexField(index)
    val links = LinkBuilder<T>().apply(builder).links
    return DataDescription(type, T::class, field, links)
}

class LinkBuilder<S : Any>(
        @PublishedApi
        internal val links: MutableList<DataLink<S, Any, Any?>> = mutableListOf()
) {

    /**
     * Links the properties of two classes, creating a [DataLink].
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any, I : Any?> link(pair: Pair<KProperty1<S, I>, KProperty1<T, I>>) {
        links.add(DataLink(pair.first, typeOf<T>(), pair.second as KProperty1<Any, I>))
    }

}