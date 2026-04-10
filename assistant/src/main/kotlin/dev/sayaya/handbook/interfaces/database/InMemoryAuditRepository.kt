package dev.sayaya.handbook.interfaces.database

import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.usecase.AuditRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
}
