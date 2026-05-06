package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.DrawerState
import dev.sayaya.handbook.client.domain.MenuRailState
import dev.sayaya.handbook.client.domain.ToolRailState
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * UC-S5: Drawer 토글
 * UC-S13: 모바일 반응형 레이아웃 (상태 전이)
 */
class DrawerModeTest : DescribeSpec({

    describe("MenuRailMode(데스크탑)는") {
        it("드로어 EXPAND 시 EXPAND 상태가 된다") {
            computeMenuRailState(DrawerState.EXPAND, hasNoChildren = true) shouldBe MenuRailState.EXPAND
        }
        it("드로어 HIDE 시 HIDE 상태가 된다") {
            computeMenuRailState(DrawerState.HIDE, hasNoChildren = true) shouldBe MenuRailState.HIDE
        }
        it("드로어 COLLAPSE + 하위 도구 없음 시 COLLAPSE 상태가 된다") {
            computeMenuRailState(DrawerState.COLLAPSE, hasNoChildren = true) shouldBe MenuRailState.COLLAPSE
        }
        it("드로어 COLLAPSE + 하위 도구 있음 시 HIDE 상태가 된다") {
            computeMenuRailState(DrawerState.COLLAPSE, hasNoChildren = false) shouldBe MenuRailState.HIDE
        }
        it("드로어 OVERLAY 시 EXPAND 상태가 된다") {
            computeMenuRailState(DrawerState.OVERLAY, hasNoChildren = true) shouldBe MenuRailState.EXPAND
        }
    }

    describe("MenuRailMode(모바일)는 도구 개수에 따라 드릴인 스왑된다") {
        listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
            it("드로어=$ds + 도구 없음 → EXPAND (MenuRail 이 [mobile] 하단 바 차지)") {
                computeMenuRailState(ds, hasNoChildren = true, mobile = true) shouldBe MenuRailState.EXPAND
            }
            it("드로어=$ds + 도구 있음(>1) → HIDE (ToolRail 이 하단 바 자리를 차지, 드릴인)") {
                computeMenuRailState(ds, hasNoChildren = false, mobile = true) shouldBe MenuRailState.HIDE
            }
        }
    }

    describe("ToolRailMode(데스크탑)는") {
        it("드로어 HIDE 시 항상 HIDE") {
            computeToolRailState(DrawerState.HIDE, MenuRailState.EXPAND, hasMultipleChildren = true) shouldBe ToolRailState.HIDE
        }
        it("드로어 EXPAND + 도구 2개 이상 시 EXPAND") {
            computeToolRailState(DrawerState.EXPAND, MenuRailState.EXPAND, hasMultipleChildren = true) shouldBe ToolRailState.EXPAND
        }
        it("드로어 EXPAND + 도구 1개 이하 시 HIDE") {
            computeToolRailState(DrawerState.EXPAND, MenuRailState.EXPAND, hasMultipleChildren = false) shouldBe ToolRailState.HIDE
        }
        it("드로어 COLLAPSE + 메뉴 COLLAPSE 상태에서도 도구가 2개 이상이면 EXPAND (peeking)") {
            computeToolRailState(DrawerState.COLLAPSE, MenuRailState.COLLAPSE, hasMultipleChildren = true) shouldBe ToolRailState.EXPAND
        }
        it("드로어 COLLAPSE + 메뉴 COLLAPSE + 도구 1개 이하 시 HIDE") {
            computeToolRailState(DrawerState.COLLAPSE, MenuRailState.COLLAPSE, hasMultipleChildren = false) shouldBe ToolRailState.HIDE
        }
        it("드로어 COLLAPSE + 메뉴 HIDE + 도구 2개 이상 시 EXPAND (peeking)") {
            computeToolRailState(DrawerState.COLLAPSE, MenuRailState.HIDE, hasMultipleChildren = true) shouldBe ToolRailState.EXPAND
        }
        it("드로어 OVERLAY + 도구 2개 이상 시 EXPAND") {
            computeToolRailState(DrawerState.OVERLAY, MenuRailState.EXPAND, hasMultipleChildren = true) shouldBe ToolRailState.EXPAND
        }
        it("드로어 OVERLAY + 도구 1개 이하 시 HIDE") {
            computeToolRailState(DrawerState.OVERLAY, MenuRailState.EXPAND, hasMultipleChildren = false) shouldBe ToolRailState.HIDE
        }
    }

    describe("ToolRailMode(모바일)는 드릴인 패턴으로") {
        it("도구 2개 이상이면 EXPAND — MenuRail 을 대신해 하단 바 자리를 차지 ([mobile] 속성이 레이아웃 담당)") {
            listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
                computeToolRailState(ds, MenuRailState.HIDE, hasMultipleChildren = true, mobile = true) shouldBe ToolRailState.EXPAND
            }
        }
        it("도구 1개 이하면 HIDE — MenuRail 이 하단 바 자리를 유지") {
            listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
                computeToolRailState(ds, MenuRailState.EXPAND, hasMultipleChildren = false, mobile = true) shouldBe ToolRailState.HIDE
            }
        }
    }

    describe("드릴인 ↔ 드릴백 전이는 MenuRail 과 ToolRail 이 상호 배타적으로 동작한다") {
        it("드릴인: 도구 없음 상태에서 도구가 생기면 Menu=EXPAND → HIDE, Tool=HIDE → EXPAND") {
            // 초기 상태 (도구 없음)
            computeMenuRailState(DrawerState.HIDE, hasNoChildren = true, mobile = true) shouldBe MenuRailState.EXPAND
            computeToolRailState(DrawerState.HIDE, MenuRailState.EXPAND, hasMultipleChildren = false, mobile = true) shouldBe ToolRailState.HIDE
            // 메뉴 선택 후 (도구 여러 개)
            computeMenuRailState(DrawerState.HIDE, hasNoChildren = false, mobile = true) shouldBe MenuRailState.HIDE
            computeToolRailState(DrawerState.HIDE, MenuRailState.HIDE, hasMultipleChildren = true, mobile = true) shouldBe ToolRailState.EXPAND
        }
        it("드릴백: CloseToolRailButton 이 MenuSelected 를 null 로 만들면 도구 목록이 비어 원 상태로 복귀") {
            // 드릴인 상태
            computeMenuRailState(DrawerState.HIDE, hasNoChildren = false, mobile = true) shouldBe MenuRailState.HIDE
            computeToolRailState(DrawerState.HIDE, MenuRailState.HIDE, hasMultipleChildren = true, mobile = true) shouldBe ToolRailState.EXPAND
            // MenuSelected.next(null) → ToolList empty → hasNoChildren=true
            computeMenuRailState(DrawerState.HIDE, hasNoChildren = true, mobile = true) shouldBe MenuRailState.EXPAND
            computeToolRailState(DrawerState.HIDE, MenuRailState.EXPAND, hasMultipleChildren = false, mobile = true) shouldBe ToolRailState.HIDE
        }
        it("한 번에 한 컨텍스트만 하단 바에 노출된다 — 두 rail 이 동시에 EXPAND 가 되지 않는다") {
            // 도구 없음 → Menu=EXPAND, Tool=HIDE
            val menu1 = computeMenuRailState(DrawerState.HIDE, hasNoChildren = true, mobile = true)
            val tool1 = computeToolRailState(DrawerState.HIDE, menu1, hasMultipleChildren = false, mobile = true)
            (menu1 == MenuRailState.EXPAND && tool1 == ToolRailState.EXPAND) shouldBe false
            menu1 shouldBe MenuRailState.EXPAND
            tool1 shouldBe ToolRailState.HIDE
            // 도구 있음 → Menu=HIDE, Tool=EXPAND
            val menu2 = computeMenuRailState(DrawerState.HIDE, hasNoChildren = false, mobile = true)
            val tool2 = computeToolRailState(DrawerState.HIDE, menu2, hasMultipleChildren = true, mobile = true)
            (menu2 == MenuRailState.EXPAND && tool2 == ToolRailState.EXPAND) shouldBe false
            menu2 shouldBe MenuRailState.HIDE
            tool2 shouldBe ToolRailState.EXPAND
        }
    }
}) {
    companion object {
        // MenuRailMode 상태 계산 로직 (GWT 의존성 없이 테스트).
        // mobile=true 에서는 도구가 여러 개이면 HIDE (드릴인 시 ToolRail 이 하단 바를 차지),
        // 도구 1개 이하이면 EXPAND — 실제 "하단 바" 형태는 CSS .rail[mobile] 속성이 담당한다.
        fun computeMenuRailState(drawerState: DrawerState, hasNoChildren: Boolean, mobile: Boolean = false): MenuRailState {
            if (mobile) return if (hasNoChildren) MenuRailState.EXPAND else MenuRailState.HIDE
            return when (drawerState) {
                DrawerState.EXPAND -> MenuRailState.EXPAND
                DrawerState.HIDE -> MenuRailState.HIDE
                DrawerState.COLLAPSE -> if (hasNoChildren) MenuRailState.COLLAPSE else MenuRailState.HIDE
                DrawerState.OVERLAY -> MenuRailState.EXPAND
            }
        }

        // ToolRailMode 상태 계산 로직. mobile=true 면 도구 개수만 본다:
        // 도구>1 → EXPAND (드릴인), 아니면 HIDE. 모바일 레이아웃은 CSS [mobile] 이 담당.
        fun computeToolRailState(drawerState: DrawerState, menuState: MenuRailState, hasMultipleChildren: Boolean, mobile: Boolean = false): ToolRailState {
            if (mobile) return if (hasMultipleChildren) ToolRailState.EXPAND else ToolRailState.HIDE
            return when (drawerState) {
                DrawerState.HIDE -> ToolRailState.HIDE
                DrawerState.EXPAND -> if (hasMultipleChildren) ToolRailState.EXPAND else ToolRailState.HIDE
                DrawerState.OVERLAY -> if (hasMultipleChildren) ToolRailState.EXPAND else ToolRailState.HIDE
                DrawerState.COLLAPSE -> {
                    // 2026-05-05: peeking 지원. 
                    // 드로어가 접혀있더라도 호버 등에 의해 도구가 생겼다면(hasMultipleChildren) 
                    // 해당 도구들을 노출(EXPAND)한다.
                    if (hasMultipleChildren) ToolRailState.EXPAND
                    else ToolRailState.HIDE
                }
            }
        }
    }
}
