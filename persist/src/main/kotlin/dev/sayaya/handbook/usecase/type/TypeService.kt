package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TypeService(
    private val repo: TypeRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    fun save(type: Type): Mono<Type> = repo.save(type)
        .flatMap { savedType -> eventHandler.publish(savedType) }
        .map { it.type }
}