package com.gitlab.kordlib.cache.annotation

import com.gitlab.kordlib.cache.api.data.IndexField
import kotlin.reflect.KClass

/**
 * Creates a one-way link with the [to] class on the [name] field,
 * meaning that upon the removal of an entity of the annotated class, all entities of the [to] class with a [name] field
 * value equal to this annotated field will also be removed.
 *
 * ```kotlin
 * data class User(@Identity @Link(to = UserMessage::class, name = "userId") val id: Long)
 * data class UserMessage(@Identity val id: Long, val userId: Long)
 *
 * suspend fun example(cache: DataCache) {
 *   val user = User(0)
 *   val willBeRemoved = UserMessage(15, user.id) //userId == user.id, so upon removal of user this value will be removed
 *   val wontBeRemoved = UserMessage(16, 20)
 *
 *   cache.put(user)
 *   cache.putAll(willBeRemoved, wontBeRemoved)
 *   cache.find<User> { User::id eq user.id }.remove()
 *   val shouldBeRemoved = cache.find<UserMessage> { UserMessage::id eq willBeRemoved.id }.singleOrNull()
 *
 *   assert(shouldBeRemoved == null)
 * }
 * ```
 */
@Repeatable
@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Link(val to: KClass<*>, val name: String)

/**
 * Creates a [IndexField] for the given class, [DataCaches][com.gitlab.kordlib.cache.api.DataCache] can use this field
 * to efficiently index entities for queries on this field. Only one Identity field is allowed per class.
 *
 * Compound keys can be created as follows:
 * ```kotlin
 * data class CompoundKey(val id1: String, val id2: String)
 *
 * val CompoundKey.id get() = "$id1:$id2"
 * ```
 *
 * Do note that using compound keys like this does not allow for indexing of the separate fields,
 * queries on `id1` or `id2` will not be eligible for optimization.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Identity
