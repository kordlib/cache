package com.gitlab.kordlib.cache.api

import kotlin.reflect.KProperty1

/**
 * A builder containing predicates to filter data with.
 */
interface QueryBuilder<T : Any> {

    /**
     * Includes only values for which the given property equals the given [value].
     */
    infix fun <R> KProperty1<T, R>.eq(value: R) = predicate { it == value }

    /**
     * Includes only values for which the given property does not equal the given [value].
     */
    infix fun <R> KProperty1<T, R>.ne(value: R) = predicate { it != value }

    /**
     * Includes only values for which the given property is lesser than the given [value].
     */
    infix fun <R : Comparable<R>> KProperty1<T, R>.lt(value: R) = predicate { it < value }

    /**
     * Includes only values for which the given property is greater than the given [value].
     */
    infix fun <R : Comparable<R>> KProperty1<T, R>.gt(value: R) = predicate { it > value }

    /**
     * Includes only values for which the given property is lesser than or equal to the given [value].
     */
    infix fun <R : Comparable<R>> KProperty1<T, R>.lte(value: R) = predicate { it <= value }

    /**
     * Includes only values for which the given property is greater than or equal to the given [value].
     */
    infix fun <R : Comparable<R>> KProperty1<T, R>.gte(value: R) = predicate { it >= value }

    /**
     * Includes only values for which the given property is inside the given [items].
     */
    infix fun <R> KProperty1<T, R>.`in`(items: Iterable<R>) = predicate { it in items }

    /**
     * Includes only values for which the given property is not inside the given [items].
     */
    infix fun <R> KProperty1<T, R>.notIn(items: Iterable<R>) = predicate { it !in items }

    /**
     * Includes only values for which the given property starts with the given [prefix].
     */
    fun <R : CharSequence> KProperty1<T, R>.startsWith(prefix: CharSequence, ignoreCase: Boolean = false) = predicate { it.startsWith(prefix, ignoreCase) }

    /**
     * Includes only values for which the given property ends with the given [postFix].
     */
    fun <R : CharSequence> KProperty1<T, R>.endsWith(postFix: CharSequence, ignoreCase: Boolean = false) = predicate { it.endsWith(postFix, ignoreCase) }

    /**
     * Includes only values for which the given property contains the given [text].
     */
    fun <R : CharSequence> KProperty1<T, R>.contains(text: CharSequence, ignoreCase: Boolean = false) = predicate { it.contains(text, ignoreCase) }

    /**
     * Includes only values for which the given [predicate] returns true.
     */
    infix fun <R> KProperty1<T, R>.predicate(predicate: (R) -> Boolean)

    /**
     * Builds a new [Query] based on the called functions.
     */
    fun build(): Query<T>

    companion object {

        private val none = object : QueryBuilder<Any> {
            override fun <R> KProperty1<Any, R>.predicate(predicate: (R) -> Boolean) {}
            override fun build(): Query<Any> = Query.none()
        }

        /**
         * Returns a builder with no results.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> none(): QueryBuilder<T> = none as QueryBuilder<T>

    }
}