package dev.sayaya.handbook.domain.event

import java.io.Serializable
import java.util.UUID

interface Event<T: Serializable, N: Event<T, N>>: Serializable {
    fun id(): UUID
    fun type(): Type
    fun param(): T
    enum class Type {
    }
}