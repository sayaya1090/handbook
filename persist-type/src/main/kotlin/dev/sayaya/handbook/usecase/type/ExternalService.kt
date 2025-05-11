package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

interface ExternalService {
    fun publish(principal: Principal, workspace: UUID, types: Map<TypeKey, Type?>): Mono<Void>
    data class TypeKey (
        val id: String,
        val version: String
    ) {
        companion object {
            fun of(type: Type): TypeKey = TypeKey(type.id, type.version)
        }
    }
}