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

    "MenuRailState는 3개의 가시성 상태만 가진다 (모바일 여부는 [mobile] 속성이 담당)" {
        MenuRailState.values().size shouldBe 3
        MenuRailState.valueOf("COLLAPSE") shouldBe MenuRailState.COLLAPSE
        MenuRailState.valueOf("EXPAND") shouldBe MenuRailState.EXPAND
        MenuRailState.valueOf("HIDE") shouldBe MenuRailState.HIDE
    }

    "ToolRailState는 3개의 가시성 상태만 가진다 (모바일 여부는 [mobile] 속성이 담당)" {
        ToolRailState.values().size shouldBe 3
        ToolRailState.valueOf("COLLAPSE") shouldBe ToolRailState.COLLAPSE
        ToolRailState.valueOf("EXPAND") shouldBe ToolRailState.EXPAND
        ToolRailState.valueOf("HIDE") shouldBe ToolRailState.HIDE
    }
})
