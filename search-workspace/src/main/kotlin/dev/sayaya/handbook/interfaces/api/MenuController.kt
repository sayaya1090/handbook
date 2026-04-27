package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.SessionStateKind
import dev.sayaya.handbook.domain.Tool
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.security.Principal

/**
 * 워크스페이스 도메인의 `/menus` 공급자.
 *
 * **역할:** shell-ui 가 Drawer 에 표시할 "workspaces" 메뉴 엔트리를
 * gateway `MenuService` 가 집계하는 `application/vnd.sayaya.handbook.v1+json`
 * 형식으로 제공한다.
 *
 * **책임:** 정적 `Menu` 정의 반환 (DB 조회 없음). 인증 불필요.
 *
 * **에이전트 연동:**
 * - **외부 AI (Tool Use)**: `/v3/api-docs` 에 OpenAPI 스펙 노출. AI 가 이 스펙의
 *   `summary` / `description` 을 읽고 function calling 으로 워크스페이스 메뉴
 *   구조(title/order/tools/url 정규식) 파악 가능.
 * - **내부 assistant**: `AGENT_COMMAND` `navigate` 의 target.menu="workspaces"
 *   로 이 메뉴를 선택할 수 있다. `UrlBasedMenuResolver` 가 `^workspaces` URL
 *   매칭으로 자동 선택.
 * - **감사**: 인증 불필요이므로 감사 대상 아님. 단 외부 AI 호출 시 gateway 가
 *   `caller_type=EXTERNAL_AGENT` AuditEntry 를 발행 (gateway 책임).
 *
 * **의존관계:**
 * - `activity` 모듈의 [Menu] / [Tool] 빌더 (Lombok `@Builder`)
 *
 * **주의:** `bottom=true` 로 드로어 하단 고정. 조회 로직(GET 권한/그룹/정보)이
 * 추가되면 이 모듈의 별도 컨트롤러로 확장한다 — `persist-workspace` 는 CUD 전용.
 */
@RestController
@Tag(
    name = "Workspace Menu",
    description = "Workspace 도메인의 /menus 공급자. shell-ui Drawer 하단 엔트리와 " +
        "AI 에이전트가 워크스페이스 관련 화면을 navigate 할 때 참조하는 메타데이터를 제공한다."
)
class MenuController {
    companion object {
        val MENU: Menu = Menu.builder()
            .title("workspaces")
            .supportingText("Name, Group, and Permission")
            .order("S")
            .icon("fa-briefcase")
            .iconType("sharp")
            .script("/js/workspace/workspace.nocache.js")
            .bottom(true)
            .tools(
                Tool.builder().title("workspace info").order("S1").icon("fa-information").iconType("sharp")
                    .url("/workspace/{workspaceId}/info").urls("^/workspace/\\{workspaceId\\}(/info)?$").build(),
                Tool.builder().title("groups").order("S5").icon("fa-users-gear").iconType("sharp")
                    .url("/workspace/{workspaceId}/groups").urls("^/workspace/\\{workspaceId\\}/groups$").build(),
                Tool.builder().title("permissions").order("S9").icon("fa-key").iconType("sharp")
                    .url("/workspace/{workspaceId}/permissions").urls("^/workspace/\\{workspaceId\\}/permissions$").build(),
            ).url("/workspace/{workspaceId}").urls("^/workspace/\\{workspaceId\\}(/.*)?$")
            // 워크스페이스 관리 메뉴는 활성 워크스페이스 선택 후에만 의미가 있음.
            // 계층 추론 없음 — AUTHENTICATED 만 선언하면 IN_WORKSPACE 에서 안 보이므로 두 값 다 필요하진 않지만,
            // 이 메뉴는 IN_WORKSPACE 전용이라 단일 선언으로 충분.
            .allowedSessionStates(SessionStateKind.AUTHENTICATED, SessionStateKind.IN_WORKSPACE)
            .build()
    }

    @Operation(
        summary = "Fetch workspace menu entries",
        description = "Returns the Workspace drawer menu (title, tools, URL regex, icon) consumed by " +
            "the shell-ui and aggregated by the gateway. External AI agents can use this endpoint to " +
            "discover available workspace-related screens and tools (info / groups / permissions) " +
            "before issuing navigate commands. The response is static (no DB lookup) and is cached " +
            "per-gateway-request with a 1200ms timeout. **Unauthenticated callers receive an empty " +
            "list** — workspace-scoped menus are only meaningful after login.",
    )
    @GetMapping(value = ["/menus"], produces = ["application/vnd.sayaya.handbook.v1+json"])
    @ResponseStatus(HttpStatus.OK)
    fun menus(principal: Principal?): Flux<Menu> =
        if (principal == null) Flux.empty() else Flux.just(MENU)
}
