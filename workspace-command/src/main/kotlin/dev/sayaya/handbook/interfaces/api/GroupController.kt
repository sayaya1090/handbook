package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.usecase.GroupService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * 워크스페이스 그룹 및 멤버 관리 REST 컨트롤러.
 *
 * **역할:** 그룹 CRUD 및 그룹 내 멤버(사용자) 배정 엔드포인트 제공.
 *
 * **권한:** 각 엔드포인트는 `{workspace}:group:*` 또는 `{workspace}:user:*` 권한이 필요하다 (Gateway Filter 에서 검증).
 */
@RestController
@RequestMapping("/workspaces/{ws}")
@Tag(name = "Group Management", description = "워크스페이스 내 그룹 및 멤버 관리 API")
class GroupController(private val svc: GroupService) {

    @Operation(summary = "그룹 생성")
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    fun createGroup(
        @PathVariable("ws") workspaceId: UUID,
        @RequestBody request: CreateGroupRequest
    ): Mono<Group> = svc.createGroup(workspaceId, request.name, request.description)

    @Operation(summary = "그룹 삭제")
    @DeleteMapping("/groups/{gid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteGroup(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID
    ): Mono<Void> = svc.deleteGroup(workspaceId, groupId)

    @Operation(summary = "멤버 추가")
    @PostMapping("/groups/{gid}/members/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addMember(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID,
        @PathVariable("uid") userId: UUID
    ): Mono<Void> = svc.addMember(workspaceId, groupId, userId)

    @Operation(summary = "멤버 삭제")
    @DeleteMapping("/groups/{gid}/members/{uid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID,
        @PathVariable("uid") userId: UUID
    ): Mono<Void> = svc.removeMember(workspaceId, groupId, userId)

    data class CreateGroupRequest(val name: String, val description: String? = null)
}
