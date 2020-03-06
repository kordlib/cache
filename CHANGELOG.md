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
as utility functions.

## Deprecations

`DataCache#find` and `DataEntryCache#find` have been deprecated in favour of `DataEntryCache#query`, to
more clearly communicate the cache is merely creating a query.
