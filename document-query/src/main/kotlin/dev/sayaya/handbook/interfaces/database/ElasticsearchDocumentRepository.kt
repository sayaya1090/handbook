package dev.sayaya.handbook.interfaces.database

import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * Elasticsearch 문서 검색 리포지토리.
 */
interface ElasticsearchDocumentRepository : ReactiveElasticsearchRepository<ElasticsearchDocumentEntity, UUID> {
    fun findByWorkspaceAndType(workspace: UUID, type: String): Flux<ElasticsearchDocumentEntity>
    fun findByWorkspaceAndTypeAndSerial(workspace: UUID, type: String, serial: String): Mono<ElasticsearchDocumentEntity>
    fun deleteByWorkspaceAndId(workspace: UUID, id: UUID): Mono<Void>
}
