package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Artifact
import dev.sayaya.handbook.domain.AuditEntry
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 감사 기록 저장소 포트.
 *
 * <p><b>책임:</b> AuditEntry의 CRUD 및 상태/아티팩트 업데이트를 정의한다.</p>
 */
interface AuditRepository {
    fun save(entry: AuditEntry): Mono<AuditEntry>
    fun findByWorkspace(workspace: UUID): Flux<AuditEntry>
    fun updateStatus(id: UUID, status: AuditEntry.Status): Mono<Void>
    fun updateArtifact(id: UUID, artifact: Artifact): Mono<Void>
    fun findByWorkspaceAndStatusWithArtifact(workspace: UUID, status: AuditEntry.Status): Flux<AuditEntry>
}
