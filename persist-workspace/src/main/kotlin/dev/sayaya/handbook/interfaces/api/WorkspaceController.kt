package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.usecase.WorkspaceService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.*

@RestController
@RequestMapping("/workspace")
class WorkspaceController(private val svc: WorkspaceService) {

    @PostMapping(
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun create(
        @AuthenticationPrincipal principal: Principal,
        @RequestBody param: CreateWorkspaceRequest,
    ): Mono<Workspace> = svc.create(principal, param.name, param.description)

    @PutMapping(
        value = ["/{id}"],
        consumes = ["application/vnd.sayaya.handbook.v1+json"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable id: UUID,
        @RequestBody param: UpdateWorkspaceRequest,
    ): Mono<Workspace> = svc.update(Workspace(id, param.name, param.description))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID): Mono<Void> = svc.delete(id)

    data class CreateWorkspaceRequest(val name: String, val description: String? = null)
    data class UpdateWorkspaceRequest(val name: String, val description: String? = null)
}
