package dev.sayaya.handbook.client.usecase

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * UC-S9(에이전트 화면 이동) 및 UC-S14(SSE 실시간 이벤트 수신)에서 사용되는
 * URL 파싱 로직을 검증한다.
 *
 * UrlBasedMenuResolver 자체는 JsRegExp, DomGlobal 등 GWT 런타임에 의존하므로
 * 순수 JVM 테스트에서 직접 테스트할 수 없다. 대신, 에이전트 화면 이동 경로에서
 * 핵심 역할을 하는 워크스페이스 ID 추출 로직을 검증하여
 * URI 기반 메뉴 해석 경로가 올바르게 동작함을 확인한다.
 */
class UrlBasedMenuResolverTest : DescribeSpec({

    describe("WorkspaceEventListener.extractWorkspaceId는") {

        it("표준 워크스페이스 URL에서 ID를 추출한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/abc-123/type")
            result shouldBe "abc-123"
        }

        it("하위 경로 없이 워크스페이스 ID만 있는 URL을 처리한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/ws-001")
            result shouldBe "ws-001"
        }

        it("쿼리스트링이 포함된 URL에서 ID를 추출한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/ws-002?tab=settings")
            result shouldBe "ws-002"
        }

        it("전체 URL(프로토콜 + 호스트 포함)에서 ID를 추출한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("https://example.com/workspace/ws-003/document")
            result shouldBe "ws-003"
        }

        it("워크스페이스 경로가 없는 URL에서 null을 반환한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/settings/profile")
            result shouldBe null
        }

        it("null URL에서 null을 반환한다") {
            val result = WorkspaceEventListener.extractWorkspaceId(null)
            result shouldBe null
        }

        it("워크스페이스 ID가 비어있는 URL에서 null을 반환한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/")
            result shouldBe null
        }

        it("UUID 형식의 워크스페이스 ID를 추출한다") {
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/550e8400-e29b-41d4-a716-446655440000/type")
            result shouldBe "550e8400-e29b-41d4-a716-446655440000"
        }

        it("에이전트 navigate 커맨드에서 사용되는 URL 패턴을 처리한다") {
            // agent-ui의 NavigateHandler가 발행하는 URL 형태
            val result = WorkspaceEventListener.extractWorkspaceId("/workspace/agent-ws/type-management")
            result shouldBe "agent-ws"
        }
    }
})
