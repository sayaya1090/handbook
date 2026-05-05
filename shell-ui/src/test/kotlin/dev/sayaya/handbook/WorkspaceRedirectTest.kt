package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import java.util.regex.Pattern

@GwtHtml("redirecttest.html")
internal class WorkspaceRedirectTest : GwtTestSpec({
    Given("WorkspaceApi 가 302 리다이렉트된 응답(redirected=true)을 받는 상황") {
        When("애플리케이션이 로드되어 워크스페이스 목록을 요청하면") {
            Then("페이지 URL 이 /workspaces/onboarding 으로 변경되어야 한다") {
                // 1. URL 변경을 대기 (최대 5초)
                page.waitForURL(Pattern.compile(".*/workspaces/onboarding$"), com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(5000.0))

                // 2. 최종 URL 검증
                val currentUrl = page.url()
                currentUrl.endsWith("/workspaces/onboarding") shouldBe true
            }
        }
    }
})
