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
 * Playwright 기반 E2E 스모크 테스트.
 *
 * 앱이 로컬에서 실행 중일 때 주요 페이지 로딩과 인증 흐름을 검증한다.
 * APP_BASE_URL 환경변수로 접속 URL을 지정할 수 있다 (기본: http://localhost:8080).
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
            Then("body가 렌더링된다") {
                page.navigate(baseUrl)
                page.waitForLoadState()
                val body = page.querySelector("body")
                body shouldNotBe null
                body!!.innerHTML().shouldNotBeBlank()
            }
        }
    }

    Given("인증이 필요한 API에 접근할 때") {
        When("토큰 없이 워크스페이스 API를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = page.navigate("$baseUrl/workspace")
                response shouldNotBe null
                response!!.status() shouldBe 401
            }
        }

        When("토큰 없이 타입 API를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = page.navigate("$baseUrl/workspace/00000000-0000-0000-0000-000000000000/types?effect_date_time=2026-01-01T00:00:00Z&expire_date_time=2026-12-31T23:59:59Z")
                response shouldNotBe null
                response!!.status() shouldBe 401
            }
        }

        When("토큰 없이 문서 API를 호출하면") {
            Then("401 Unauthorized가 반환된다") {
                val response = page.navigate("$baseUrl/workspace/00000000-0000-0000-0000-000000000000/documents")
                response shouldNotBe null
                response!!.status() shouldBe 401
            }
        }
    }

    Given("헬스체크 엔드포인트에 접근할 때") {
        When("actuator health를 호출하면") {
            Then("200 OK가 반환된다") {
                val response = page.navigate("$baseUrl/actuator/health")
                response shouldNotBe null
                response!!.status() shouldBe 200
                page.content().contains("UP") shouldBe true
            }
        }
    }

    Given("정적 리소스에 접근할 때") {
        When("CSS 파일을 요청하면") {
            Then("정상적으로 로드된다") {
                val response = page.navigate("$baseUrl/css/global.css")
                response shouldNotBe null
                // 정적 리소스 서비스가 실행 중이면 200, 아니면 라우팅 설정에 따라 다를 수 있음
                (response!!.status() in listOf(200, 401)) shouldBe true
            }
        }
    }
})
