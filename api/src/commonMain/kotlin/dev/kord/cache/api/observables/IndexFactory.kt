package dev.kord.cache.api.observables

interface IndexFactory<Value> {
    public fun createIndexFor(value: Value): Index
}