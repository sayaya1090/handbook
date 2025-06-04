package dev.sayaya.handbook.`interface`.database

import com.fasterxml.jackson.databind.ObjectMapper
import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.usecase.DocumentRepository
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
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

    private fun toDomain(entity: R2dbcDocumentEntity): Document = Document (
        id = entity.id,
        type = entity.type,
        serial = entity.serial,
        effectDateTime = entity.effectDateTime,
        expireDateTime = entity.expireDateTime,
        createDateTime = entity.createDateTime,
        creator = entity.creatorUserName,
        data = objectMapper.readValue(entity.data.asArray(), Map::class.java) as Map<String, String?>,
    )
}

/*
@Query("SELECT d FROM Document d " +
           "JOIN Type t ON d.workspace.id = t.workspace.id AND d.type = t.name " + // Document와 Type을 workspace ID와 type 이름으로 JOIN
           "WHERE t.workspace.id = :workspaceId " + // Type의 workspace ID 조건
           "  AND t.name = :typeName " +             // Type의 이름 조건
           "  AND t.last = true " +                  // Type의 최신 버전(last=true)을 사용
           "  AND d.effectDateTime < t.expireDateTime " + // Document의 시작일이 Type의 종료일보다 이전이고
           "  AND d.expireDateTime > t.effectDateTime")   // Document의 종료일이 Type의 시작일보다 이후인 경우 (기간 겹침)

 */
