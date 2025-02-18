package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import reactor.core.publisher.Mono
import java.security.Principal

interface ExternalService {
    fun publish(type: Type, principal: Principal): Mono<Void>
}