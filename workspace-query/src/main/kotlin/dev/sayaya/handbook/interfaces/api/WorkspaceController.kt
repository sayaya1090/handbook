package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Workspace
import dev.sayaya.handbook.interfaces.authentication.UserAuthentication
import dev.sayaya.handbook.usecase.WorkspaceSearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * 워크스페이스 read-only REST 컨트롤러.
 *
 * **역할:** 워크스페이스 목록 · 단건 조회 엔드포인트를 제공한다.
 *
 * **에이전트 연동:**
 * - **외부 AI (Tool Use)**: `GET /workspaces` 를 function calling 으로 호출하여 사용자가
 *   속한(추정) 워크스페이스 목록을 조회 → 후속 tool 호출의 `workspace` 파라미터 결정 근거
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
            "group (including the auto-created admin group). External AI agents call this first " +
            "to discover available workspace IDs before issuing domain-specific tool calls " +
            "(e.g. list_types, search_documents). The service runs on a PostgreSQL session forced " +
            "to `default_transaction_read_only=on`, so accidental writes are rejected by the DB.",
    )
    @GetMapping(
        value = ["/workspaces"],
        produces = ["application/vnd.sayaya.handbook.v1+json"],
    )
    @ResponseStatus(HttpStatus.OK)
    fun list(
        @AuthenticationPrincipal authentication: UserAuthentication,
    ): Flux<Workspace> {
        // Phase 1a: sub(사용자 UUID) 우선, 없으면 id(jti 이전 토큰에서는 사용자 UUID)로 폴백.
        val userId = authentication.sub ?: authentication.id
            ?: return Flux.empty()
        val userUuid = runCatching { UUID.fromString(userId) }.getOrNull()
            ?: return Flux.empty()
        return service.listForUser(userUuid)
    }

    @Operation(
        summary = "Get workspace by id (read-only)",
        description = "Returns a single workspace by its UUID. 404 when the workspace is not found " +
            "or not visible to the caller.",
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
