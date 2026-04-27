package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Group
import dev.sayaya.handbook.domain.User
import dev.sayaya.handbook.usecase.GroupSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.util.*

@RestController
@Tag(name = "Group Search", description = "그룹 및 멤버 조회 전용 API")
class GroupSearchController(private val svc: GroupSearchService) {

    @Operation(summary = "워크스페이스 그룹 목록 조회")
    @GetMapping(
        value = ["/workspace/{ws}/groups"],
        produces = ["application/vnd.sayaya.handbook.v1+json"]
    )
    @ResponseStatus(HttpStatus.OK)
    fun listGroups(@PathVariable("ws") workspaceId: UUID): Flux<Group> =
        svc.listGroups(workspaceId)

    @Operation(summary = "그룹 멤버 목록 조회")
    @GetMapping(
        value = ["/workspace/{ws}/groups/{gid}/members"],
        produces = ["application/vnd.sayaya.handbook.v1+json"]
    )
    @ResponseStatus(HttpStatus.OK)
    fun listMembers(
        @PathVariable("ws") workspaceId: UUID,
        @PathVariable("gid") groupId: UUID
    ): Flux<User> = svc.listMembers(workspaceId, groupId)
}
