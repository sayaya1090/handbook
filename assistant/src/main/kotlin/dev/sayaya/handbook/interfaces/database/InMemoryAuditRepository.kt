package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.Artifact
import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.usecase.AuditRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 인메모리 감사 기록 저장소.
 *
 * <p><b>책임:</b> 테스트 및 개발용 AuditRepository 구현체.
 * ConcurrentHashMap을 사용하여 스레드 안전하게 AuditEntry를 관리한다.</p>
 *
 * <p><b>주의:</b> 프로세스 재시작 시 모든 데이터가 소실된다. 프로덕션 환경에서는 사용 금지.</p>
 */
class InMemoryAuditRepository : AuditRepository {
    private val store = ConcurrentHashMap<UUID, AuditEntry>()

    override fun save(entry: AuditEntry): Mono<AuditEntry> = Mono.fromCallable {
        store[entry.id] = entry
        entry
    }

    override fun findByWorkspace(workspace: UUID): Flux<AuditEntry> = Flux.defer {
        Flux.fromIterable(
            store.values
                .filter { it.workspace == workspace }
                .sortedByDescending { it.timestamp }
        )
    }

    override fun updateStatus(id: UUID, status: AuditEntry.Status): Mono<Void> = Mono.fromRunnable {
        store.computeIfPresent(id) { _, entry -> entry.copy(status = status) }
    }

    override fun updateArtifact(id: UUID, artifact: Artifact): Mono<Void> = Mono.fromRunnable {
        store.computeIfPresent(id) { _, entry -> entry.copy(artifact = artifact) }
    }

    override fun findByWorkspaceAndStatusWithArtifact(workspace: UUID, status: AuditEntry.Status): Flux<AuditEntry> = Flux.defer {
        Flux.fromIterable(
            store.values
                .filter { it.workspace == workspace && it.status == status && it.artifact != null }
                .sortedByDescending { it.timestamp }
        )
    }
}
