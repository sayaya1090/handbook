package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.usecase.AuditRepository
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
@RequestMapping("/assistant/audit")
class AuditController(private val repo: AuditRepository) {
    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun list(@RequestParam workspace: UUID): Flux<AuditEntry> = repo.findByWorkspace(workspace)
}
