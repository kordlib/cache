# DataCache

An adaptable cache that allows query-like operations.

```kotlin
class Person(val id: Int, val age: Int, val name: String)
/**
 * Generates an object that tells caches how to handle this type.
 * Here we indicated that instances of MyType should be indexed on their `id` field.
 */
val description = description(Person::id) 

val cache = MapDataCache() //A `DataCache` that's backed by a ConcurrentMap. Every `DataCache` can support an arbitrary number of types.
 
cache.register(description)  //Types need to be registered before a cache can use them, this provides an initial setup.

cache.put(Person(500, 24, "Test Dummy"))

val entry = cache.find<Person> { Person::id eq 500 }.single()
cache.find<Person>().update { it.copy(age = it.age + 1) }
cache.find<Person>().remove()   
```

## Cascading

`DataCache` supports cascading of entities linked by properties.

```kotlin
class User(val id: Int, val name: String)
class Message(val id: Int, val userId: Int, val content: String)

val userDescription = description(User::id) {
    link(User::id to Message::userId) //will remove messages with the same userId as the removed User's id.
}
val messageDescription = description(Message::id)

val cache = MapDataCache {
    forType<Message> { lruHashMap(100) } //only keep the latest 100 messages
}

cache.register(userDescription, messageDescription)
cache.put(User(500, "Test Dummy"))
cache.put(Message(400, 500, "Hello world"))

cache.find<User> { User::id eq 500 }.remove()
val messages = cache.find<Message>().count()

assert(messages == 0)
```

## Annotation processor

`DataDescriptions` can be automatically generated with the `annotation-processor` module.

```kotlin
//creates `userDescription` and `messageDescription`

class User(
    @Identity
    @Link(Message::class, "userId")
    val id: Int, 
    val name: String
)

class Message(
    @Identity
    val id: Int, 
    val userId: Int, 
    val content: String
)
```

Note that there is currently an [issue](https://youtrack.jetbrains.com/issue/KT-34189)
with kapt relating to repeatable annotations. Because of that entities with
multiple `@Link` annotations on the same property will not generate those links.