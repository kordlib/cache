# Map

A `DataCache` implementation using [Redis Lettuce](https://lettuce.io/). 
Data entries are serialized with [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization).

```kotlin
@Serializable
data class Example(val id: Long, val thing: String) {
    companion object {
        val description = description(Example::id)
    }
}

val configuration = RedisConfiguration {//defaults
    client = RedisClient.create("redis://localhost")
    
    //protobuf cannot encode `null`. If you do have nullable fields, set their default value
    //to null and disable `encodeDefaults`.
    binaryFormat = ProtoBuf(encodeDefaults = false) 
    reuseConnection = true
    keyPrefix = "dev:kord:cache"
}

val cache = DelegatingDataCache {

    default { cache, description -> 
        RedisEntryCache(cache, description, configuration) 
    }

    forDescription(Example.description) { cache, description ->
        RedisEntryCache(
                cache,
                description,
                RedisConfiguration(),
                Example.serializer(),
                keySerializer = { it.toString().toUtf8Bytes() },
                entryName = "Examples"
        )
    }
}
```
