package dev.sayaya.handbook.e2e

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * Playwright 기반 E2E 테스트 스켈레톤.
 * 앱이 로컬에서 실행 중일 때 동작한다.
 */
class DocumentFlowTest : BehaviorSpec({
    val baseUrl = System.getenv("APP_BASE_URL") ?: "http://localhost:8080"

    lateinit var playwright: Playwright
    lateinit var browser: Browser
    lateinit var page: Page

    beforeSpec {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true),
        )
    }

    afterSpec {
        browser.close()
        playwright.close()
    }

    beforeTest {
        page = browser.newPage()
    }

    afterTest { (_, _) ->
        page.close()
    }

    Given("앱이 실행 중일 때") {
        When("메인 페이지에 접속하면") {
            Then("페이지가 정상적으로 로드된다") {
                page.navigate(baseUrl)
                page.title().shouldNotBeBlank()
            }
        }

        When("페이지 로드 후") {
            Then("메뉴가 렌더링된다") {
                page.navigate(baseUrl)
                page.waitForLoadState()
                val body = page.querySelector("body")
                body shouldNotBe null
                body!!.innerHTML().shouldNotBeBlank()
            }
        }
    }
})
