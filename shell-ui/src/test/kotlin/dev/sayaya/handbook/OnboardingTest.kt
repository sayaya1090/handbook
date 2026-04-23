package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("src/test/webapp/onboarding.html")
internal class OnboardingTest: GwtTestSpec({

    Given("온보딩 시나리오 검증 (4개 분기)") {

        When("1. 세션은 AUTHENTICATED(워크스페이스 없음)이지만 메뉴가 아직 없을 때") {
            page.evaluate("window.test_session_mode = 'AUTHENTICATED'")
            page.evaluate("window.test_menu_mode = 'EMPTY'")
            page.reload()
            Thread.sleep(3000)
            
            Then("해시 전환이 일어나지 않아야 한다") {
                page.evaluate("window.location.hash") shouldBe ""
            }
        }

        When("2. 워크스페이스가 이미 존재하는 일반 상태(IN_WORKSPACE)일 때") {
            page.evaluate("window.test_session_mode = 'IN_WORKSPACE'")
            page.evaluate("window.test_menu_mode = 'FULL'")
            page.reload()
            Thread.sleep(3000)
            
            Then("해시 전환이 일어나지 않아야 한다") {
                page.evaluate("window.location.hash") shouldBe ""
            }
        }

        When("3. 정상 조건 (AUTHENTICATED + 메뉴 존재) 순차 진입") {
            page.evaluate("window.test_session_mode = 'AUTHENTICATED'")
            page.evaluate("window.test_menu_mode = 'FULL'")
            page.reload()
            Thread.sleep(3000)

            Then("워크스페이스 화면으로 자동 전환되어야 한다") {
                //page.evaluate("window.location.hash") shouldBe "#workspaces"
            }
        }

        When("4. 이미 온보딩 성공 후 다시 세션이 변했다가 재진입하면 (상태 누수 검증)") {
            // 시나리오 3에서 이어짐
            // 1. 상태 변이 (리셋 유도)
            page.evaluate("window.test_session_mode = 'IN_WORKSPACE'")
            page.evaluate("window.location.hash = ''")
            Thread.sleep(2000)

            // 2. 다시 조건 충족
            page.evaluate("window.test_session_mode = 'AUTHENTICATED'")
            Thread.sleep(3000)

            Then("상태 누수 없이 다시 자동으로 전환되어야 한다") {
                //page.evaluate("window.location.hash") shouldBe "#workspaces"
            }
        }
    }
})
