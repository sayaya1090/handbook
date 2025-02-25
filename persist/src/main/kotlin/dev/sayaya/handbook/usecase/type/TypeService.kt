package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.Principal

@Service
class TypeService(
    private val repo: TypeRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    fun save(type: Type, principal: Principal): Mono<Type> = repo.save(type)
        .flatMap { savedType -> eventHandler.publish(savedType, principal) }
        .map { it.type }
}