package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/dashboardtest.html")
internal class DashboardTest: GwtTestSpec({
    Given("대시보드 UI가 초기화됨") {
        Thread.sleep(3000)

        // 통계 카드 검증
        Then("대시보드 컨테이너가 존재한다") {
            page.querySelector(".dash-container") shouldNotBe null
        }
        Then("통계 카드 행이 존재한다") {
            page.querySelector(".dash-stats-row") shouldNotBe null
        }
        Then("통계 카드 3개가 렌더링된다") {
            val cards = page.querySelectorAll(".dash-stat-card")
            cards.count() shouldBe 3
        }
        Then("타입 수가 표시된다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values.count() shouldBe 3
        }

        // 품질 패널 검증
        Then("품질 패널이 존재한다") {
            page.querySelector(".dash-quality-panel") shouldNotBe null
        }
        Then("품질 이슈 목록이 존재한다") {
            page.querySelector(".dash-quality-list") shouldNotBe null
        }
        Then("품질 이슈 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-quality-row")
            rows.count() shouldBe 2
        }
        Then("severity 배지가 표시된다") {
            page.querySelector(".dash-severity-badge") shouldNotBe null
        }

        // 에이전트 활동 로그 검증
        Then("에이전트 활동 패널이 존재한다") {
            page.querySelector(".dash-activity-panel") shouldNotBe null
        }
        Then("에이전트 활동 목록이 존재한다") {
            page.querySelector(".dash-activity-list") shouldNotBe null
        }
        Then("에이전트 활동 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-activity-row")
            rows.count() shouldBe 2
        }
        Then("활동 시간이 표시된다") {
            page.querySelector(".dash-activity-time") shouldNotBe null
        }
        Then("활동 상태가 표시된다") {
            page.querySelector(".dash-activity-status") shouldNotBe null
        }
    }
})
