package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceBuilder
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal

@RestController
class WorkspaceController(private val svc: WorkspaceService) {
    @PostMapping("/workspace", produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun save(@AuthenticationPrincipal principal: Principal, @RequestBody param: WorkspaceBuilder): Mono<Workspace> = svc.save(principal, param)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): Mono<String> {
        ex.printStackTrace()
        return Mono.justOrEmpty(ex.localizedMessage)
    }
}