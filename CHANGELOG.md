# 0.1.1

## Additions

Added `DataCache#flow`, `DataCache#remove`, `DataCache#count` and their respective `DataEntryCache` variants 
as utility functions.

## Deprecations

`DataCache#find` and `DataEntryCache#find` have been deprecated in favour of `DataEntryCache#query`, to
more clearly communicate the cache is merely creating a query.
