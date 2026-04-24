package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("src/test/webapp/historytest.html")
internal class HistoryTest: GwtTestSpec({

    Given("HistoryManager가 초기화됨") {

        When("uri 스트림에 새로운 경로 '/test-path'를 입력하면") {
            // 브릿지 함수 사용
            page.evaluate("window.test_uri_next('/test-path')")
            Thread.sleep(500)

            Then("브라우저 주소창의 pathname이 '/test-path'로 변경되어야 한다") {
                page.evaluate("window.location.pathname") shouldBe "/test-path"
            }
        }

        When("MenuSelected를 통해 새로운 메뉴를 입력하면") {
            // 브릿지 함수 사용 (JsProperty name인 url_regex 사용)
            page.evaluate("""
                var menu = {
                    title: 'Test Menu',
                    url_regex: ['/menu-path']
                };
                window.test_menu_select(menu);
            """)
            Thread.sleep(500)

            Then("브라우저 주소창의 pathname이 '/menu-path'로 변경되어야 한다") {
                page.evaluate("window.location.pathname") shouldBe "/menu-path"
            }
        }

        When("브라우저의 위치를 '/direct-path'로 직접 변경하고 popstate를 발생시키면") {
            // 사용자 제안: 코틀린에서 윈도우 로케이션 직접 변경
            page.evaluate("""
                history.pushState(null, '', '/direct-path');
                window.dispatchEvent(new PopStateEvent('popstate'));
            """)
            Thread.sleep(500)

            Then("uri 스트림의 현재 값이 '/direct-path'로 업데이트되어야 한다") {
                // 노출된 프리미티브 값(current_uri) 확인
                page.evaluate("window.current_uri") shouldBe "/direct-path"
            }
        }
    }
})
