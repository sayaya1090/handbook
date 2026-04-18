package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-S21 (UC-12 빈 워크스페이스 자동 온보딩) Playwright 테스트.
 *
 * WorkspaceList 의 초기값은 empty (`List.of()`) 이므로 페이지 로드 직후 Bootstrapper 가
 * 가상 onboarding Menu 를 MenuSelected 에 push 한다. 그 이후 User 를 non-empty 로 교체하거나
 * 다시 empty 로 되돌려도 `loaded` 플래그로 1회만 발화해야 한다.
 */
@GwtHtml("src/test/webapp/onboarding.html")
internal class WorkspaceOnboardingTest: GwtTestSpec({
    Given("페이지 로드 직후 (WorkspaceList 초기 empty)") {
        Thread.sleep(1000)

        Then("MenuSelected 가 onboarding Menu 로 1회 push 되어 있다") {
            val count = page.querySelector("#push-count")
            count shouldNotBe null
            count!!.textContent() shouldBe "1"
        }
        Then("Menu.title 이 i18n 키 'workspace.onboarding' 로 설정된다") {
            page.querySelector("#selected-menu-title")!!.textContent() shouldBe "workspace.onboarding"
        }
        Then("Menu.script 가 workspace-ui nocache 경로로 설정된다") {
            page.querySelector("#selected-menu-script")!!.textContent() shouldBe "js/workspace/workspace.nocache.js"
        }
        Then("Menu.icon 이 fa-circle-plus 로 설정된다") {
            page.querySelector("#selected-menu-icon")!!.textContent() shouldBe "fa-circle-plus"
        }
        Then("Menu.iconType 이 solid 로 설정된다") {
            page.querySelector("#selected-menu-icon-type")!!.textContent() shouldBe "solid"
        }
        Then("Menu.order 가 '0' 으로 설정된다") {
            page.querySelector("#selected-menu-order")!!.textContent() shouldBe "0"
        }
    }

    Given("non-empty User 를 push 하면") {
        page.click("#emit-non-empty")
        Thread.sleep(300)

        Then("Bootstrapper 는 재발화하지 않는다 (push-count 유지)") {
            page.querySelector("#push-count")!!.textContent() shouldBe "1"
        }
        Then("MenuSelected 는 여전히 onboarding Menu 상태이다") {
            // UrlBasedMenuResolver 가 없는 독립 테스트 셸이므로 URL 기반 복귀는 검증 범위 밖.
            // Bootstrapper 단독 책임 — non-empty 수신 시 신규 push 안 함.
            page.querySelector("#selected-menu-title")!!.textContent() shouldBe "workspace.onboarding"
        }
    }

    Given("empty User 를 다시 push 해도") {
        page.click("#emit-empty")
        Thread.sleep(300)

        Then("loaded 가드로 재발화 안 한다 (push-count 유지)") {
            page.querySelector("#push-count")!!.textContent() shouldBe "1"
        }
    }

    Given("empty → non-empty → empty 순회 후에도") {
        page.click("#emit-non-empty")
        Thread.sleep(100)
        page.click("#emit-empty")
        Thread.sleep(300)

        Then("push-count 는 여전히 1 — 최초 발화 1회만 유지") {
            page.querySelector("#push-count")!!.textContent() shouldBe "1"
        }
    }
})
