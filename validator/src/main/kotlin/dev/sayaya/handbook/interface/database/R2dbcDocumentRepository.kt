package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.Validation
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Repository
class R2dbcDocumentRepository(
    private val template: R2dbcEntityTemplate,
    private val objectMapper: ObjectMapper
): DocumentRepository {
    override fun findByType(workspace: UUID, type: String, effectDateTime: Instant, expireDateTime: Instant): Flux<Document> = template.select(
        query(where("workspace").`is`(workspace)
            .and("type").`is`(type)
            .and("effective_at").lessThan(expireDateTime)
            .and("expire_at").greaterThan(effectDateTime)
            .and("last").`is`(true) // 최신 버전만 조회
        ), R2dbcDocumentEntity::class.java
    ).map(::toDomain)

    override fun findById(workspace: UUID, id: UUID): Mono<Document> = template.select(
        query(where("workspace").`is`(workspace)
            .and("id").`is`(id)
            .and("last").`is`(true) // 최신 버전만 조회
        ), R2dbcDocumentEntity::class.java
    ).singleOrEmpty().map(::toDomain)

    private fun toDomain(entity: R2dbcDocumentEntity): Document = Document (
        id = entity.id,
        type = entity.type,
        serial = entity.serial,
        effectDateTime = entity.effectDateTime,
        expireDateTime = entity.expireDateTime,
        createDateTime = entity.createDateTime,
        creator = entity.creatorUserName,
        data = objectMapper.readValue(entity.data.asArray(), Map::class.java) as Map<String, String?>,
        validations = toValidation(entity)
    )
    private fun toValidation(entity: R2dbcDocumentEntity): Validation? = if(entity.validationStatus==null) null else Validation(
        status = Validation.Companion.Status.valueOf(entity.validationStatus),
        result = if(entity.validationResult==null) null else objectMapper.readValue(entity.validationResult.asArray(), Map::class.java) as Map<String, Boolean>
    )
}