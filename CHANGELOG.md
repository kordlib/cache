# 0.1.4

## Changes

* `DataCache#find` and `DataCache#query` no longer throw a KNPE when an entry was not previously registered.
Instead, an empty query (`Query.none`) will be returned.

# Version

Kotlin 1.3.61 -> 1.4.10
Gradle 5.5.1 -> 6.6.1

# 0.1.3

version bump to move to new repository, from now on cache will be hosted on the same repository as our other Kord projects.
```groovy
repositories {
    maven { url "https://dl.bintray.com/kordlib/Kord" }
}
```

# 0.1.2

## Additions

Added `DataCache#flow`, `DataCache#remove`, `DataCache#count` and their respective `DataEntryCache` variants 
as utility functions.__

## Deprecations

`DataCache#find` and `DataEntryCache#find` have been deprecated in favour of `DataEntryCache#query`, to
more clearly communicate the cache is merely creating a query.
