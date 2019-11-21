package com.gitlab.kordlib.cache.api.meta

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.QueryBuilder
import com.gitlab.kordlib.cache.api.query.Query
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KType

fun DataCache.withMetrics(): MetricsCache = MetricsCache(this)

class MetricsCache(
        private val delegate: DataCache,
        private val logger: StatisticsLogger = StatisticsLogger()
) : DataCache by delegate {

    val statistics: CacheStatistics get() = logger.metaData

    override fun <T : Any> getEntry(type: KType): DataEntryCache<T>? {
        val cache = getEntry<T>(type) ?: return null
        return MetricsEntryCache(cache, logger.getForType(type))
    }

}

private class MetricsEntryCache<T : Any>(
        private val delegate: DataEntryCache<T>,
        private val logger: TypeStatisticsLogger
) : DataEntryCache<T> by delegate {

    override fun query(): QueryBuilder<T> {
        val builder = delegate.query()
        return MetricsQueryBuilder(builder, logger)
    }

}

private class MetricsQueryBuilder<T : Any>(
        private val delegate: QueryBuilder<T>,
        private val logger: TypeStatisticsLogger
) : QueryBuilder<T> by delegate {

    override fun build(): Query<T> {
        val query = delegate.build()
        return MetricsQuery(query, logger)
    }

}

private class MetricsQuery<V : Any>(
        private val delegate: Query<V>,
        private val logger: TypeStatisticsLogger
) : Query<V> by delegate {

    override fun asFlow(): Flow<V> {
        logger.logQuery()
        val first = AtomicBoolean(false) //atomicfu causes compilation error here
        return delegate.asFlow().onEach {
            if (first.compareAndSet(false, true)) logger.logHit()
        }
    }

    override suspend fun single(): V {
        logger.logQuery()
        val result = runCatching { delegate.single() }

        result.onSuccess { logger.logQuery() }
        return result.getOrElse { throw it }
    }

    override suspend fun singleOrNull(): V? {
        logger.logQuery()
        return delegate.singleOrNull()?.also {
            logger.logHit()
        }
    }

    override suspend fun toCollection(): Collection<V> {
        logger.logQuery()
        val collection = super.toCollection()

        if (collection.isNotEmpty()) logger.logHit()
        return collection
    }

}
