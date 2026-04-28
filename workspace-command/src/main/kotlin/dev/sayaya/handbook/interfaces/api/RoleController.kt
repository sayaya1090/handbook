package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.RoleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * 워크스페이스 역할(Role) 관리 REST 컨트롤러.
 *
 * **역할:** 그룹에 역할 부여 및 제거, 그룹별 역할 조회 엔드포인트 제공.
 *
 * **권한:** 각 엔드포인트는 `{workspace}:role:*` 권한이 필요하다.
 */
@RestController
@RequestMapping("/workspaces/{ws}/groups/{gid}/roles")
@Tag(name = "Role Management", description = "그룹별 역할(RBAC) 관리 API")
class RoleController(private val svc: RoleService) {

    @Operation(summary = "그룹 역할 조회")
    @GetMapping
    fun getRoles(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID
    ): Flux<String> = svc.getRoles(workspaceId, groupId)

    @Operation(summary = "그룹 역할 부여")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignRole(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID,
        @RequestBody request: AssignRoleRequest
    ): Mono<Void> = svc.assignRole(workspaceId, groupId, request.roleName)

    @Operation(summary = "그룹 역할 제거")
    @DeleteMapping("/{role}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeRole(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID,
        @PathVariable("role") roleName: String
    ): Mono<Void> = svc.removeRole(workspaceId, groupId, roleName)

    data class AssignRoleRequest(val roleName: String)
}
