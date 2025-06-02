package dev.sayaya.handbook.`interface`.database

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.ValidationTaskRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class R2dbcValidationTaskRepository (
    private val template: R2dbcEntityTemplate,
): ValidationTaskRepository {
    override fun expire(documents: List<Document>): Mono<Void> = Mono.empty()
}