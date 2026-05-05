package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

/**
 * 워크스페이스 read-only REST 컨트롤러.
 *
 * **역할:** 워크스페이스 목록 · 단건 조회 엔드포인트를 제공한다.
 * 사용자의 워크스페이스가 0개인 경우 온보딩(UC-12)을 위해 302 리다이렉트를 수행한다.
 *
 * **에이전트 연동:**
 * - **외부 AI (Tool Use)**: `GET /workspaces` 를 function calling 으로 호출하여 사용자가
 *   속한(추정) 워크스페이스 목록을 조회. 만약 **302 Found** 응답을 받으면, 사용자가
 *   아직 워크스페이스가 없는 상태이므로 온보딩 절차(/workspaces/onboarding)를 안내해야 함.
 * - **내부 assistant**: 자연어 "A 워크스페이스 열어줘" 류 요청 해석 시 목록에서 이름 매칭
 *   후 navigate 커맨드 발행에 활용
 * - **감사**: gateway 가 `caller_type=EXTERNAL_AGENT/USER` AuditEntry 발행 (인증 필요 경로)
 *
 * **의존관계:**
 * - [WorkspaceSearchService] — 조회 유스케이스
 */
@RestController
@Tag(
    name = "Workspace Search",
    description = "읽기 전용 워크스페이스 조회 API. AI 에이전트가 워크스페이스 컨텍스트 선택 전에 " +
        "목록을 파악하기 위해 호출하는 기본 엔드포인트."
)
class WorkspaceController(
    private val service: WorkspaceSearchService,
) {

    @Operation(
        summary = "List workspaces visible to caller (read-only)",
        description = "Returns workspaces where the authenticated principal is a member of any " +
            "group (including the auto-created admin group).",
    )
    @GetMapping(
        value = ["/workspaces"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    fun list(
        @AuthenticationPrincipal authentication: UserAuthentication,
    ): Mono<ResponseEntity<List<Workspace>>> {
        val userId = authentication.sub ?: authentication.id
            ?: return Mono.just(ResponseEntity.ok(emptyList()))

        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull()
            ?: return Mono.just(ResponseEntity.ok(emptyList()))

        return service.listForUser(userUuid)
            .collectList()
            .map { list ->
                if (list.isEmpty()) ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/workspaces/onboarding"))
                    .build()
                else ResponseEntity.ok(list)
            }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(WorkspaceController::class.java)
    }

    @Operation(
        summary = "Get workspace by id (read-only)",
        description = "Returns a single workspace by its UUID.",
    )
    @GetMapping(
        value = ["/workspaces/{id}"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun get(
        @Parameter(description = "Workspace UUID") @PathVariable id: UUID,
    ): Mono<Workspace> = service.findById(id)
}
