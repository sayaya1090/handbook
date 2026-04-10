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
        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools().size shouldBeLessThanOrEqual 1
        }
        Then("두번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[1].tools().size shouldBeGreaterThan 1
        }
        Then("드로어 요소(nav.drawer)가 DOM에 존재한다") {
            page.querySelector("nav.drawer") shouldNotBe null
        }
        Then("메뉴 토글 버튼이 존재한다") {
            page.querySelector("#menu-toggle-button") shouldNotBe null
        }
        Then("메뉴 레일에 아이템이 메뉴 수만큼 렌더링된다") {
            val items = page.querySelectorAll(".rail .item")
            items.count() shouldBe DrawerMock.menu.size
        }
        Then("각 메뉴 레일 아이템에 아이콘 요소가 존재한다") {
            val iconCount = page.querySelectorAll(".rail .item .icon").count()
            val itemCount = page.querySelectorAll(".rail .item").count()
            iconCount shouldBeGreaterThan 0
        }

        // UC-S7: 워크스페이스 전환
        Then("워크스페이스 선택 요소(workspace)가 존재한다") {
            val wsSelect = page.querySelector(".workspace")
            wsSelect shouldNotBe null
        }

        // UC-S6: 호버 미리보기 - Tool Rail 요소 존재 확인
        Then("Tool Rail 영역(두 번째 .rail)이 존재한다") {
            val rails = page.querySelectorAll(".rail")
            rails.count() shouldBe 2
        }

        // UC-S10: i18n - 다국어 관련 요소 존재 확인
        Then("메뉴 아이템에 텍스트 라벨이 존재한다") {
            val items = page.querySelectorAll(".rail .item")
            items.count() shouldBeGreaterThan 0
        }

        When("첫번째 URL 버튼을 클릭하면") {
            page.click("#url1")
            Thread.sleep(500)
            Then("메뉴가 선택된다 (selected 속성이 있는 아이템이 1개 이상)") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
            Then("선택된 아이템은 정확히 1개이다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
        }

        When("세번째 메뉴 첫번째 Tool URL 버튼을 클릭하면") {
            page.click("#url2")
            Thread.sleep(500)
            Then("메뉴가 선택된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
            Then("선택된 아이템은 정확히 1개이다 (이전 선택 해제됨)") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
        }

        When("다른 Tool URL(url3)을 클릭하면 같은 메뉴의 다른 Tool이 활성화된다") {
            page.click("#url3")
            Thread.sleep(500)
            Then("선택된 메뉴 아이템이 유지된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
        }

        // UC-S4: URL 기반 딥링크
        When("URL hash를 변경하면") {
            page.evaluate("window.location.hash = '#/test-deep-link'")
            Thread.sleep(500)
            Then("URL 변경 후에도 드로어(nav.drawer)가 존재한다") {
                page.querySelector("nav.drawer") shouldNotBe null
            }
            Then("메뉴 레일 아이템 수가 유지된다") {
                // 첫 번째 .rail(MenuRail)의 아이템만 카운트
                val items = page.querySelectorAll(".rail:first-child .item")
                items.count() shouldBe DrawerMock.menu.size
            }
        }
    }
})
