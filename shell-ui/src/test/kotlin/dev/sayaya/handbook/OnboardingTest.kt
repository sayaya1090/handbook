package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import java.util.regex.Pattern

@GwtHtml("redirecttest.html")
internal class OnboardingTest : GwtTestSpec({
    
    Given("시나리오 1: 빈 워크스페이스 상태") {
        When("애플리케이션이 로드되면") {
            // Mock 설정 (RedirectModule): redirected=true, body=[]
            Then("부트스트래퍼가 온보딩 화면(/workspaces/onboarding)으로 리다이렉트한다") {
                page.waitForURL(Pattern.compile(".*/workspaces/onboarding$"), com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(5000.0))
                val currentUrl = page.url()
                currentUrl.endsWith("/workspaces/onboarding") shouldBe true
            }
        }
    }

    Given("시나리오 2: 워크스페이스가 존재하는 상태") {
        When("애플리케이션이 로드되면") {
            // 이전 시나리오에서 리다이렉트된 상태일 수 있으므로 루트로 이동 후 파라미터 주입
            val baseUrl = page.url().substringBefore("/workspaces/")
            page.navigate("$baseUrl/redirecttest.html?workspaces=ws-1")
            
            Then("첫 번째 워크스페이스 대시보드로 자동 리다이렉트된다") {
                page.waitForURL(Pattern.compile(".*/workspaces/ws-1/dashboard$"), com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(5000.0))
                val currentUrl = page.url()
                currentUrl.endsWith("/workspaces/ws-1/dashboard") shouldBe true
            }
        }
    }
})
