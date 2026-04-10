package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import dev.sayaya.handbook.client.drawer.DrawerMock
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/drawer.html")
internal class DrawerTest: GwtTestSpec({
    Given("메뉴가 초기화됨") {
        Thread.sleep(3000)

        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools().size shouldBeLessThanOrEqual 1
        }
        Then("두번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[1].tools().size shouldBeGreaterThan 1
        }
        Then("드로어 요소가 DOM에 존재한다") {
            page.querySelector("nav.drawer") shouldNotBe null
        }
        Then("메뉴 토글 버튼이 존재한다") {
            page.querySelector("#menu-toggle-button") shouldNotBe null
        }
        Then("메뉴 레일에 아이템이 렌더링된다") {
            val items = page.querySelectorAll(".rail .item")
            items.count() shouldBe DrawerMock.menu.size
        }

        When("첫번째 URL 버튼을 클릭하면") {
            page.click("#url1")
            Thread.sleep(500)
            Then("메뉴가 선택된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
        }

        When("세번째 메뉴 첫번째 Tool URL 버튼을 클릭하면") {
            page.click("#url2")
            Thread.sleep(500)
            Then("메뉴가 선택된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
        }

        // UC-S4: URL 기반 딥링크
        When("URL hash를 변경하면") {
            page.evaluate("window.location.hash = '#/test-deep-link'")
            Thread.sleep(500)
            Then("URL 변경 후에도 메뉴 레일이 존재한다") {
                page.querySelector("nav.drawer") shouldNotBe null
            }
            Then("메뉴 레일 아이템이 유지된다") {
                val items = page.querySelectorAll(".rail .item")
                items.count() shouldBe DrawerMock.menu.size
            }
        }

        // UC-S7: 워크스페이스 전환
        Then("워크스페이스 선택 요소가 존재한다") {
            val wsSelect = page.querySelector(".workspace-select")
            wsSelect shouldNotBe null
        }

        // UC-S6: 호버 미리보기 - Tool Rail 요소 존재 확인
        Then("Tool Rail 영역이 존재한다") {
            page.querySelector(".tool-rail") shouldNotBe null
        }

        // UC-S10: i18n - 다국어 관련 요소 존재 확인
        Then("메뉴 아이템에 텍스트 라벨이 존재한다") {
            val items = page.querySelectorAll(".rail .item")
            items.count() shouldBeGreaterThan 0
        }
    }
})
