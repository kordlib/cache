package com.gitlab.kordlib.cache.api


interface DataEntryCache<VALUE : Any> {

    fun query(): QueryBuilder<VALUE>

    suspend fun put(item: VALUE)

    companion object {
        fun <T : Any> none() = object : DataEntryCache<T> {
            override suspend fun put(item: T) {}
            override fun query(): QueryBuilder<T> = QueryBuilder.none()
        }
    }
}

inline fun <reified T : Any> DataEntryCache<T>.find(block: QueryBuilder<T>.() -> Unit = {}) =
        query().apply(block).build()
