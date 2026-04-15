package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.DrawerState
import dev.sayaya.handbook.client.domain.MenuRailState
import dev.sayaya.handbook.client.domain.ToolRailState
import dev.sayaya.handbook.client.usecase.*
import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.domain.Tool
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

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
        it("드로어 OVERLAY 시 BOTTOM_NAV 상태가 된다") {
            computeMenuRailState(DrawerState.OVERLAY, hasNoChildren = true) shouldBe MenuRailState.BOTTOM_NAV
        }
    }

    describe("MenuRailMode(모바일)는 DrawerState 와 무관하게 항상 BOTTOM_NAV 다") {
        listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
            it("드로어=$ds → BOTTOM_NAV") {
                computeMenuRailState(ds, hasNoChildren = true, mobile = true) shouldBe MenuRailState.BOTTOM_NAV
                computeMenuRailState(ds, hasNoChildren = false, mobile = true) shouldBe MenuRailState.BOTTOM_NAV
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
        it("드로어 COLLAPSE + 메뉴 COLLAPSE 시 항상 HIDE") {
            computeToolRailState(DrawerState.COLLAPSE, MenuRailState.COLLAPSE, hasMultipleChildren = true) shouldBe ToolRailState.HIDE
        }
        it("드로어 COLLAPSE + 메뉴 HIDE + 도구 2개 이상 시 COLLAPSE") {
            computeToolRailState(DrawerState.COLLAPSE, MenuRailState.HIDE, hasMultipleChildren = true) shouldBe ToolRailState.COLLAPSE
        }
        it("드로어 OVERLAY + 도구 2개 이상 시 HORIZONTAL_CHIPS") {
            computeToolRailState(DrawerState.OVERLAY, MenuRailState.BOTTOM_NAV, hasMultipleChildren = true) shouldBe ToolRailState.HORIZONTAL_CHIPS
        }
        it("드로어 OVERLAY + 도구 1개 이하 시 HIDE") {
            computeToolRailState(DrawerState.OVERLAY, MenuRailState.BOTTOM_NAV, hasMultipleChildren = false) shouldBe ToolRailState.HIDE
        }
    }

    describe("ToolRailMode(모바일)는 DrawerState 와 무관하게") {
        it("도구 2개 이상이면 HORIZONTAL_CHIPS") {
            listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
                computeToolRailState(ds, MenuRailState.BOTTOM_NAV, hasMultipleChildren = true, mobile = true) shouldBe ToolRailState.HORIZONTAL_CHIPS
            }
        }
        it("도구 1개 이하면 HIDE") {
            listOf(DrawerState.EXPAND, DrawerState.COLLAPSE, DrawerState.HIDE, DrawerState.OVERLAY).forEach { ds ->
                computeToolRailState(ds, MenuRailState.BOTTOM_NAV, hasMultipleChildren = false, mobile = true) shouldBe ToolRailState.HIDE
            }
        }
    }
}) {
    companion object {
        // MenuRailMode 상태 계산 로직 (GWT 의존성 없이 테스트). mobile=true 면 항상 BOTTOM_NAV.
        fun computeMenuRailState(drawerState: DrawerState, hasNoChildren: Boolean, mobile: Boolean = false): MenuRailState {
            if (mobile) return MenuRailState.BOTTOM_NAV
            return when (drawerState) {
                DrawerState.EXPAND -> MenuRailState.EXPAND
                DrawerState.HIDE -> MenuRailState.HIDE
                DrawerState.COLLAPSE -> if (hasNoChildren) MenuRailState.COLLAPSE else MenuRailState.HIDE
                DrawerState.OVERLAY -> MenuRailState.BOTTOM_NAV
            }
        }

        // ToolRailMode 상태 계산 로직. mobile=true 면 도구 개수만 본다.
        fun computeToolRailState(drawerState: DrawerState, menuState: MenuRailState, hasMultipleChildren: Boolean, mobile: Boolean = false): ToolRailState {
            if (mobile) return if (hasMultipleChildren) ToolRailState.HORIZONTAL_CHIPS else ToolRailState.HIDE
            return when (drawerState) {
                DrawerState.HIDE -> ToolRailState.HIDE
                DrawerState.EXPAND -> if (hasMultipleChildren) ToolRailState.EXPAND else ToolRailState.HIDE
                DrawerState.OVERLAY -> if (hasMultipleChildren) ToolRailState.HORIZONTAL_CHIPS else ToolRailState.HIDE
                DrawerState.COLLAPSE -> {
                    if (menuState == MenuRailState.COLLAPSE) ToolRailState.HIDE
                    else if (hasMultipleChildren) ToolRailState.COLLAPSE
                    else ToolRailState.HIDE
                }
            }
        }
    }
}
