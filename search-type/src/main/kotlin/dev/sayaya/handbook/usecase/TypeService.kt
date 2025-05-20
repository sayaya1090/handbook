package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Type
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Service
class TypeService(private val repo: TypeRepository) {
    fun findByRange(workspace: UUID, effectDateTime: Instant?, expireDateTime: Instant?): Flux<Type> = if(effectDateTime!=null) repo.findByRange(workspace, effectDateTime, expireDateTime ?: effectDateTime)
    else findAll(workspace)
    private fun findAll(workspace: UUID): Flux<Type> = repo.findAll(workspace)
}