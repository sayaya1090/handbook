package dev.sayaya.handbook.interfaces.database

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.Instant
import java.util.*

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
    fun toDomain(): dev.sayaya.handbook.domain.Document {
        val map = java.lang.reflect.Proxy.newProxyInstance(
            jsinterop.base.JsPropertyMap::class.java.classLoader,
            arrayOf(jsinterop.base.JsPropertyMap::class.java)
        ) { _, method, args ->
            when (method.name) {
                "get" -> data[args[0] as String]?.toString()
                "set" -> null
                "forEach" -> null
                else -> null
            }
        } as jsinterop.base.JsPropertyMap<String>
        
        val doc = dev.sayaya.handbook.domain.Document.create(
            id.toString(), type, serial,
            effectDateTime.toEpochMilli().toDouble(),
            expireDateTime.toEpochMilli().toDouble(),
            createDateTime?.toEpochMilli()?.toDouble() ?: 0.0,
            creator,
            map
        )
        doc.status(status)
        doc.rev(rev)
        return doc
    }

    companion object {
        fun fromDomain(workspace: UUID, doc: dev.sayaya.handbook.domain.Document): ElasticsearchDocumentEntity {
            val om = tools.jackson.module.kotlin.jacksonObjectMapper()
            val dataJson = om.writeValueAsString(doc.data() ?: emptyMap<String, Any>())
            val dataMap = om.readValue(dataJson, object : tools.jackson.core.type.TypeReference<Map<String, Any?>>() {})
            
            return ElasticsearchDocumentEntity(
                id = doc.id()?.let { UUID.fromString(it) } ?: UUID.randomUUID(),
                workspace = workspace,
                type = doc.type(),
                serial = doc.serial(),
                effectDateTime = Instant.ofEpochMilli(doc.effectDateTime().toLong()),
                expireDateTime = Instant.ofEpochMilli(doc.expireDateTime().toLong()),
                createDateTime = if (doc.createDateTime() > 0) Instant.ofEpochMilli(doc.createDateTime().toLong()) else null,
                creator = doc.creator(),
                data = dataMap,
                status = doc.status() ?: "DRAFT",
                rev = doc.rev()
            )
        }
    }
}
