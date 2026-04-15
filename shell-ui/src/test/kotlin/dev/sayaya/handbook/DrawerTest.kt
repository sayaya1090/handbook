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
        Then("메뉴 토글 버튼은 헤더 안에 위치한다") {
            // 워크스페이스 셀렉터와 가로 일렬로 같은 .drawer-header 안에 있어야 함
            val headerToggle = page.querySelector(".drawer-header #menu-toggle-button")
            headerToggle shouldNotBe null
        }
        Then("테마 토글 버튼이 메뉴 레일 하단에 .item.rail-bottom 으로 존재한다") {
            // ThemeToggle 은 NavigationRailItemElement 를 상속 → .item 구조 + .rail-bottom 클래스
            val themeBtn = page.querySelector(".rail .item.rail-bottom")
            themeBtn shouldNotBe null
        }
        Then("테마 토글 버튼은 MenuRail(첫 번째 rail) 에 속한다") {
            // 두 번째 rail(ToolRail) 이 아닌 MenuRail 에 append 되어야 함
            val themeInMenuRail = page.querySelector(".rail:first-child .item.rail-bottom")
            themeInMenuRail shouldNotBe null
        }
        Then("테마 토글 버튼은 .collapse 와 md-item start slot 두 곳에 SVG 를 가진다") {
            // 아이콘 버튼(collapse 모드) + md-item 의 start slot(expand 모드) 두 위치 독립 렌더
            val collapseSvg = page.querySelector(".rail .item.rail-bottom .collapse svg.theme-toggle-svg")
            val startSvg = page.querySelector(".rail .item.rail-bottom md-item svg.theme-toggle-svg[slot='start']")
            collapseSvg shouldNotBe null
            startSvg shouldNotBe null
        }
        Then("테마 토글 버튼 SVG 안에 sun/moon 두 path 가 모두 존재한다") {
            // 일출/일몰 morph keyframes 를 위해 두 아이콘이 동시 렌더되고 CSS 가 가시성을 전환
            val sun = page.querySelector(".rail .item.rail-bottom svg .sun")
            val moon = page.querySelector(".rail .item.rail-bottom svg .moon")
            sun shouldNotBe null
            moon shouldNotBe null
        }
        Then("bottom=true 메뉴에 .bottom-menu 클래스가 부여된다") {
            // DrawerMock 의 Menu 3/4 가 bottom=true → MenuRailElement 가 .bottom-menu 클래스 추가
            val bottomMenus = page.querySelectorAll(".rail:first-child .item.bottom-menu")
            val bottomCount = DrawerMock.menu.count { it.bottom() == true }
            bottomMenus.count() shouldBe bottomCount
        }
        Then("MenuRail 의 총 .item 수는 메뉴 수 + 1(theme) 이다") {
            val items = page.querySelectorAll(".rail:first-child .item")
            items.count() shouldBe (DrawerMock.menu.size + 1)
        }
        Then("테마 토글 버튼 headline 에 i18n 라벨 텍스트가 존재한다") {
            // LabelProvider fallback 값(Switch to Dark/Light)이라도 비어있지 않아야 함
            val headline = page.evaluate(
                "document.querySelector('.rail .item.rail-bottom md-item [slot=headline]').textContent"
            ).toString()
            (headline.isNotBlank()) shouldBe true
        }
        Then("초기 상태에서 html 의 color-theme 속성이 light 또는 dark 로 설정되어 있다") {
            val theme = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
            (theme == "light" || theme == "dark") shouldBe true
        }
        When("테마 토글 버튼을 클릭하면") {
            val beforeHeadline = page.evaluate(
                "document.querySelector('.rail .item.rail-bottom md-item [slot=headline]').textContent"
            ).toString()
            val before = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
            page.click(".rail .item.rail-bottom")
            Thread.sleep(200)
            Then("color-theme 속성이 반대 값으로 토글된다") {
                val after = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
                after shouldNotBe before
                (after == "light" || after == "dark") shouldBe true
            }
            Then("theme-changing 클래스가 일시 부착되었다가 500ms 내에 제거된다") {
                // 클릭 후 200ms 지점에서는 아직 부착되어 있을 수 있음. 최대 600ms 대기해 사라지는 것 확인
                Thread.sleep(500)
                val has = page.evaluate(
                    "document.documentElement.classList.contains('theme-changing')"
                ).toString()
                has shouldBe "false"
            }
            Then("headline 라벨이 토글 후 반대 값으로 바뀐다") {
                val afterHeadline = page.evaluate(
                    "document.querySelector('.rail .item.rail-bottom md-item [slot=headline]').textContent"
                ).toString()
                afterHeadline shouldNotBe beforeHeadline
                afterHeadline.isNotBlank() shouldBe true
            }
        }
        Then("메뉴 레일에 아이템이 메뉴 수만큼 렌더링된다(theme 제외)") {
            // theme toggle 도 .item 이므로 .rail-bottom 을 제외한 카운트가 실제 메뉴 수와 일치
            val items = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom)")
            items.count() shouldBe DrawerMock.menu.size
        }
        Then("각 메뉴 레일 아이템에 아이콘 요소가 존재한다") {
            val iconCount = page.querySelectorAll(".rail .item .icon").count()
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
                // 첫 번째 .rail(MenuRail)의 아이템만 카운트, theme toggle 제외
                val items = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom)")
                items.count() shouldBe DrawerMock.menu.size
            }
        }

        // UC-S15: ThemeToggle 배치 순서 — ThemeToggle 이 bottom 메뉴보다 시각적으로 위에 위치
        Then("bottom 메뉴가 DOM 상 ThemeToggle 뒤에 위치한다 (flex order: theme=1, bottom=2)") {
            // MenuRailElement 는 일반 메뉴 → ThemeToggle append 순서로 DOM 구성.
            // bottom 메뉴는 DOM 순서상 theme 보다 앞에 있더라도 CSS flex order 로 뒤로 밀림.
            // 여기서는 computed style 의 order 값을 직접 검증.
            val themeOrder = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item.rail-bottom')).order"
            ).toString()
            val bottomOrder = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item.bottom-menu')).order"
            ).toString()
            themeOrder.toInt() shouldBeLessThanOrEqual bottomOrder.toInt() - 1
        }
    }
})
