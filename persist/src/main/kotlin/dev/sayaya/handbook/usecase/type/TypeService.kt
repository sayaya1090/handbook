package dev.sayaya.handbook.usecase.type

import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class TypeService(
    private val repo: TypeRepository,
    private val eventHandler: ExternalServiceHandler,
) {
    fun save(workspace: UUID, type: Type): Mono<Type> = repo.save(workspace, type)
        .flatMap { savedType -> eventHandler.publish(workspace, savedType) }
        .map { it.type }
}