package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("onboardingtest.html")
internal class OnboardingTest : GwtTestSpec({
    
    Given("시나리오 1: 빈 워크스페이스 상태") {
        When("애플리케이션이 로드되면") {
            // Mock 설정: WorkspaceList 가 비어있음
            // 부트스트래퍼가 비어있는 목록 감지 후 온보딩 메뉴를 추가하는지 검증
            Then("부트스트래퍼가 온보딩 스크립트를 로드한다") {
                // 검증: module-script ID를 가진 스크립트가 DOM에 존재해야 함
            }
        }
    }

    Given("시나리오 2: 워크스페이스가 존재하는 상태") {
        When("애플리케이션이 로드되면") {
            // Mock 설정: WorkspaceList 에 데이터가 있음
            Then("첫 번째 워크스페이스 대시보드로 자동 리다이렉트된다") {
                // 검증: URL이 /workspaces/{id}/dashboard 로 변경됨
            }
        }
    }
})
