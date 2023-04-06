package dev.kord.cache.api.observables

interface Index: Comparable<Index> {
    override fun hashCode(): Int
}