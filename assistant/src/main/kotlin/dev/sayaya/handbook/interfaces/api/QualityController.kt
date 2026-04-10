package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.QualityMonitorService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/assistant/quality")
class QualityController(private val service: QualityMonitorService) {
    @PostMapping("/scan")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun scan(@RequestParam workspace: UUID): Mono<Void> = service.execute(workspace)
}
