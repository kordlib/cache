# Map

A `DataCache` implementation backed by Kotlin's and Java's maps.

```kotlin
val cache: DataCache = MapDataCache {
    default { none() }
    forType<MyEntity> { concurrentHashMap() }
}
```