package com.gitlab.kordlib.cache

import com.gitlab.kordlib.cache.api.Identity
import com.gitlab.kordlib.cache.api.Link
import custom.path.to.descriptions.userDescription
import custom.path.to.descriptions.userMessageDescription
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class User(@Link(to = UserMessage::class, name = "id") @Identity val userId: Long)
class UserMessage(@Identity val id: Long)

@Identity
val CompoundEntity.id get() = "$id1:$id2"

class CompoundEntity(val id1: Long, val id2: Long)

class EntityTest {

    private val description1 = userDescription
    private val description2 = userMessageDescription

    @Test
    @ExperimentalStdlibApi
    fun `processor correctly generates links`() {
        Assertions.assertEquals(1, description1.links.size)
        val link = description1.links.single()

        Assertions.assertEquals(typeOf<UserMessage>(), link.target)

        Assertions.assertEquals(0, description2.links.size)

    }


}

