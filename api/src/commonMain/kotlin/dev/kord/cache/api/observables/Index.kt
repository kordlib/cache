package dev.kord.cache.api.observables

typealias IndexFactory<Value> = (value: Value) -> Index
interface Index: Comparable<Index> {
    override fun hashCode(): Int
}