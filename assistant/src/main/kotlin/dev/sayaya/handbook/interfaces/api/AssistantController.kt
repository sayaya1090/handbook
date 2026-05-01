package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.AuditEntry
import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.domain.ExecutionRequest
import dev.sayaya.handbook.usecase.AssistantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * AI 어시스턴트 REST API 컨트롤러.
 *
 * <p><b>책임:</b> 클라이언트의 HTTP 요청을 AssistantService로 위임하고,
 * 실행 ID 기반의 다중 실행 관리 API를 제공한다.</p>
 *
 * <p><b>의존관계:</b>
 * <ul>
 *   <li>{@link AssistantService} — 어시스턴트 비즈니스 로직</li>
 * </ul></p>
 *
 * <p><b>주의:</b> request()가 반환하는 executionId를 후속 execute/respond/abort 호출에 사용해야 한다.</p>
 */
@RestController
@RequestMapping("/assistant")
class AssistantController(private val assistantService: AssistantService) {

    @PostMapping(
        "/request",
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    fun request(@RequestParam workspace: UUID, @RequestBody body: Map<String, String>): Mono<ExecutionRequest> {
        val message = body["message"] ?: return Mono.error(IllegalArgumentException("message is required"))
        return assistantService.request(workspace, message)
    }

    @PostMapping(
        "/execute",
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun execute(
        @RequestParam workspace: UUID,
        @RequestParam executionId: UUID,
        @RequestBody plan: ExecutionPlan,
    ): Mono<Void> = assistantService.execute(workspace, executionId, plan)

    @PostMapping(
        "/respond",
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun respond(
        @RequestParam workspace: UUID,
        @RequestParam executionId: UUID,
        @RequestBody body: Map<String, Any>,
    ): Mono<Void> {
        val response = body["response"]?.toString() ?: return Mono.error(IllegalArgumentException("response is required"))
        return assistantService.respond(workspace, executionId, response)
    }

    @PostMapping("/abort")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun abort(@RequestParam executionId: UUID): Mono<Void> = assistantService.abort(executionId)

    @GetMapping(
        "/executions",
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    fun getExecutions(@RequestParam workspace: UUID): Flux<Map<String, Any>> =
        assistantService.getExecutions(workspace)

    @GetMapping(
        "/artifacts",
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    fun getArtifacts(@RequestParam workspace: UUID): Flux<AuditEntry> =
        assistantService.getArtifacts(workspace)
}
