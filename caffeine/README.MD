# Map

A `DataCache` implementation backed by [Caffeine(https://github.com/ben-manes/caffeine)

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