package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.usecase.AuditRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*

@RestController
@RequestMapping("/assistant/audit")
class AuditController(private val repo: AuditRepository) {
    @GetMapping(produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun list(@RequestParam workspace: UUID): Flux<AuditEntry> = repo.findByWorkspace(workspace)
}
