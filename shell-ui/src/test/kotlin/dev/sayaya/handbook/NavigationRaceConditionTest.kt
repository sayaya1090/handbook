package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

@GwtHtml("historytest.html")
internal class NavigationRaceConditionTest: GwtTestSpec({

    Given("NavigationManager와 PlaceholderResolver가 초기화됨") {
        page.onConsoleMessage { println("BROWSER CONSOLE: ${it.text()}") }
        
        When("세션 컨텍스트에 workspaceId가 없는 상태에서 {workspaceId}가 포함된 메뉴를 선택하면") {
            page.evaluate("""
                var menu = {
                    title: 'Dashboard',
                    url: '/workspaces/{workspaceId}/dashboard',
                    url_regex: ['^/workspaces/.+/dashboard$']
                };
                window.test_menu_select(menu);
            """)
            Thread.sleep(500)

            Then("잘못된 템플릿 URL(/workspaces/{workspaceId}/dashboard)이 주소창에 반영되지 않아야 한다") {
                val currentPath = page.evaluate("window.location.pathname") as String
                currentPath shouldBe "/historytest.html"
            }
        }
    }
})
