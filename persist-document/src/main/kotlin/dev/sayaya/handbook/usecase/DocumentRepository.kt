package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import dev.sayaya.handbook.domain.DocumentPatch
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 문서 영속화 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 문서의 일괄 저장, 부분 수정(JSONB 머지), 삭제를 정의한다.
 *
 * **의존관계:**
 * - [R2dbcDocumentRepositoryAdapter][dev.sayaya.handbook.interfaces.database.R2dbcDocumentRepositoryAdapter] — R2DBC 구현체
 */
interface DocumentRepository {
    fun saveAll(workspace: UUID, documents: List<Document>): Flux<Document>
    fun patchAll(workspace: UUID, patches: List<DocumentPatch>): Flux<Document>
    fun deleteAll(workspace: UUID, documents: List<Document>): Mono<Void>
    fun findAll(workspace: UUID, type: String?): Flux<Document>
    fun findById(id: UUID): Mono<Document>
    fun updateStatus(id: UUID, status: String): Mono<Document>
}
