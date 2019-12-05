# Map

A `DataCache` implementation backed by Kotlin's and Java's maps.

```kotlin
val cache: DataCache = CaffeineDataCache {
    default { caffeine() }
    forType<MyEntity> {
        caffeine {
            it.maximumSize(100).expireAfterAccess(Duration.ofMinutes(5))
        }
    }
}
```