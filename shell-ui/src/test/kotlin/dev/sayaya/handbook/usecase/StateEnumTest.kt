package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.DrawerState
import dev.sayaya.handbook.client.domain.MenuRailState
import dev.sayaya.handbook.client.domain.ToolRailState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StateEnumTest : StringSpec({

    "DrawerState는 4개의 상태를 가진다" {
        DrawerState.values().size shouldBe 4
        DrawerState.valueOf("COLLAPSE") shouldBe DrawerState.COLLAPSE
        DrawerState.valueOf("EXPAND") shouldBe DrawerState.EXPAND
        DrawerState.valueOf("HIDE") shouldBe DrawerState.HIDE
        DrawerState.valueOf("OVERLAY") shouldBe DrawerState.OVERLAY
    }

    "MenuRailState는 4개의 상태를 가진다" {
        MenuRailState.values().size shouldBe 4
        MenuRailState.valueOf("COLLAPSE") shouldBe MenuRailState.COLLAPSE
        MenuRailState.valueOf("EXPAND") shouldBe MenuRailState.EXPAND
        MenuRailState.valueOf("HIDE") shouldBe MenuRailState.HIDE
        MenuRailState.valueOf("BOTTOM_NAV") shouldBe MenuRailState.BOTTOM_NAV
    }

    "ToolRailState는 4개의 상태를 가진다" {
        ToolRailState.values().size shouldBe 4
        ToolRailState.valueOf("COLLAPSE") shouldBe ToolRailState.COLLAPSE
        ToolRailState.valueOf("EXPAND") shouldBe ToolRailState.EXPAND
        ToolRailState.valueOf("HIDE") shouldBe ToolRailState.HIDE
        ToolRailState.valueOf("HORIZONTAL_CHIPS") shouldBe ToolRailState.HORIZONTAL_CHIPS
    }
})
