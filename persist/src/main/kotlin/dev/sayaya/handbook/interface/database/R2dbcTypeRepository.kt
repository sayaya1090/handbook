package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class R2dbcTypeRepository: TypeRepository {
    override fun save(type: Type): Mono<Void> {
        TODO("Not yet implemented")
    }
}