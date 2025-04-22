package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceBuilder
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
class WorkspaceController(private val svc: WorkspaceService) {
    @PostMapping("/workspace", produces = ["application/vnd.sayaya.handbook.v1+json"])
    fun save(@RequestBody param: WorkspaceBuilder): Mono<Workspace> = svc.save(param)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}