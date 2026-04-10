package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.AuditEntry
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface AuditRepository {
    fun save(entry: AuditEntry): Mono<AuditEntry>
    fun findByWorkspace(workspace: UUID): Flux<AuditEntry>
    fun updateStatus(id: UUID, status: AuditEntry.Status): Mono<Void>
}
