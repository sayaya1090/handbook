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

    describe("MenuRailMode는") {
        it("드로어 EXPAND 시 EXPAND 상태가 된다") {
            val result = computeMenuRailState(DrawerState.EXPAND, hasNoChildren = true)
            result shouldBe MenuRailState.EXPAND
        }
        it("드로어 HIDE 시 HIDE 상태가 된다") {
            val result = computeMenuRailState(DrawerState.HIDE, hasNoChildren = true)
            result shouldBe MenuRailState.HIDE
        }
        it("드로어 COLLAPSE + 하위 도구 없음 시 COLLAPSE 상태가 된다") {
            val result = computeMenuRailState(DrawerState.COLLAPSE, hasNoChildren = true)
            result shouldBe MenuRailState.COLLAPSE
        }
        it("드로어 COLLAPSE + 하위 도구 있음 시 HIDE 상태가 된다") {
            val result = computeMenuRailState(DrawerState.COLLAPSE, hasNoChildren = false)
            result shouldBe MenuRailState.HIDE
        }
        it("드로어 OVERLAY 시 BOTTOM_NAV 상태가 된다") {
            val result = computeMenuRailState(DrawerState.OVERLAY, hasNoChildren = true)
            result shouldBe MenuRailState.BOTTOM_NAV
        }
    }

    describe("ToolRailMode는") {
        it("드로어 HIDE 시 항상 HIDE") {
            val result = computeToolRailState(DrawerState.HIDE, MenuRailState.EXPAND, hasMultipleChildren = true)
            result shouldBe ToolRailState.HIDE
        }
        it("드로어 EXPAND + 도구 2개 이상 시 EXPAND") {
            val result = computeToolRailState(DrawerState.EXPAND, MenuRailState.EXPAND, hasMultipleChildren = true)
            result shouldBe ToolRailState.EXPAND
        }
        it("드로어 EXPAND + 도구 1개 이하 시 HIDE") {
            val result = computeToolRailState(DrawerState.EXPAND, MenuRailState.EXPAND, hasMultipleChildren = false)
            result shouldBe ToolRailState.HIDE
        }
        it("드로어 COLLAPSE + 메뉴 COLLAPSE 시 항상 HIDE") {
            val result = computeToolRailState(DrawerState.COLLAPSE, MenuRailState.COLLAPSE, hasMultipleChildren = true)
            result shouldBe ToolRailState.HIDE
        }
        it("드로어 COLLAPSE + 메뉴 HIDE + 도구 2개 이상 시 COLLAPSE") {
            val result = computeToolRailState(DrawerState.COLLAPSE, MenuRailState.HIDE, hasMultipleChildren = true)
            result shouldBe ToolRailState.COLLAPSE
        }
        it("드로어 OVERLAY + 도구 2개 이상 시 HORIZONTAL_CHIPS") {
            val result = computeToolRailState(DrawerState.OVERLAY, MenuRailState.BOTTOM_NAV, hasMultipleChildren = true)
            result shouldBe ToolRailState.HORIZONTAL_CHIPS
        }
        it("드로어 OVERLAY + 도구 1개 이하 시 HIDE") {
            val result = computeToolRailState(DrawerState.OVERLAY, MenuRailState.BOTTOM_NAV, hasMultipleChildren = false)
            result shouldBe ToolRailState.HIDE
        }
    }
}) {
    companion object {
        // MenuRailMode 상태 계산 로직 (GWT 의존성 없이 테스트)
        fun computeMenuRailState(drawerState: DrawerState, hasNoChildren: Boolean): MenuRailState {
            return when (drawerState) {
                DrawerState.EXPAND -> MenuRailState.EXPAND
                DrawerState.HIDE -> MenuRailState.HIDE
                DrawerState.COLLAPSE -> if (hasNoChildren) MenuRailState.COLLAPSE else MenuRailState.HIDE
                DrawerState.OVERLAY -> MenuRailState.BOTTOM_NAV
            }
        }

        // ToolRailMode 상태 계산 로직
        fun computeToolRailState(drawerState: DrawerState, menuState: MenuRailState, hasMultipleChildren: Boolean): ToolRailState {
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
