package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.ExecutionPlan
import dev.sayaya.handbook.usecase.AssistantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/assistant")
class AssistantController(private val assistantService: AssistantService) {

    @PostMapping(
        "/request",
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    fun request(@RequestParam workspace: UUID, @RequestBody body: Map<String, String>): Mono<ExecutionPlan> {
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
        @RequestBody plan: ExecutionPlan,
    ): Mono<Void> = assistantService.execute(workspace, plan)

    @PostMapping(
        "/respond",
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun respond(
        @RequestParam workspace: UUID,
        @RequestBody body: Map<String, Any>,
    ): Mono<Void> {
        // For now, re-execute based on user response
        // This will be expanded when await_confirm flow is fully implemented
        return Mono.empty()
    }

    @PostMapping("/abort")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun abort(): Mono<Void> = assistantService.abort()
}
