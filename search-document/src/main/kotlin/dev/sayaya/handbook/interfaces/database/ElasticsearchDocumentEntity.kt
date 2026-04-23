package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.Instant
import java.util.UUID

/**
 * Elasticsearch 문서 검색용 엔티티.
 *
 * **역할:** Elasticsearch 'documents' 인덱스 매핑.
 */
@Document(indexName = "documents")
data class ElasticsearchDocumentEntity(
    @Id
    val id: UUID,
    
    @Field(type = FieldType.Keyword)
    val workspace: UUID,
    
    @Field(type = FieldType.Keyword)
    val type: String,
    
    @Field(type = FieldType.Keyword)
    val serial: String,
    
    @Field(type = FieldType.Date)
    val effectDateTime: Instant,
    
    @Field(type = FieldType.Date)
    val expireDateTime: Instant,
    
    @Field(type = FieldType.Date)
    val createDateTime: Instant?,
    
    @Field(type = FieldType.Keyword)
    val creator: String?,
    
    @Field(type = FieldType.Object)
    val data: Map<String, Any?>,
    
    @Field(type = FieldType.Keyword)
    val status: String,
    
    val rev: Long
) {
    fun toDomain() = dev.sayaya.handbook.domain.Document(
        id = id,
        type = type,
        serial = serial,
        effectDateTime = effectDateTime,
        expireDateTime = expireDateTime,
        createDateTime = createDateTime,
        creator = creator,
        data = data.mapValues { it.value?.toString() },
        status = status,
        rev = rev
    )

    companion object {
        fun fromDomain(workspace: UUID, doc: dev.sayaya.handbook.domain.Document) = ElasticsearchDocumentEntity(
            id = doc.id ?: UUID.randomUUID(), // id 가 없는 경우 생성 (onboarding/import 상황 고려)
            workspace = workspace,
            type = doc.type,
            serial = doc.serial,
            effectDateTime = doc.effectDateTime,
            expireDateTime = doc.expireDateTime,
            createDateTime = doc.createDateTime,
            creator = doc.creator,
            data = doc.data,
            status = doc.status,
            rev = doc.rev ?: 0L
        )
    }
}
