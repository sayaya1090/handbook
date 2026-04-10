package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.DrawerState
import dev.sayaya.handbook.client.domain.MenuRailState
import dev.sayaya.handbook.client.domain.ToolRailState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StateEnumTest : StringSpec({

    "DrawerState는 3개의 상태를 가진다" {
        DrawerState.values().size shouldBe 3
        DrawerState.valueOf("COLLAPSE") shouldBe DrawerState.COLLAPSE
        DrawerState.valueOf("EXPAND") shouldBe DrawerState.EXPAND
        DrawerState.valueOf("HIDE") shouldBe DrawerState.HIDE
    }

    "MenuRailState는 3개의 상태를 가진다" {
        MenuRailState.values().size shouldBe 3
        MenuRailState.valueOf("COLLAPSE") shouldBe MenuRailState.COLLAPSE
        MenuRailState.valueOf("EXPAND") shouldBe MenuRailState.EXPAND
        MenuRailState.valueOf("HIDE") shouldBe MenuRailState.HIDE
    }

    "ToolRailState는 3개의 상태를 가진다" {
        ToolRailState.values().size shouldBe 3
        ToolRailState.valueOf("COLLAPSE") shouldBe ToolRailState.COLLAPSE
        ToolRailState.valueOf("EXPAND") shouldBe ToolRailState.EXPAND
        ToolRailState.valueOf("HIDE") shouldBe ToolRailState.HIDE
    }
})
