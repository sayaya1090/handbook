package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("dashboardtest.html")
internal class DashboardStatsTest: GwtTestSpec({
    Given("대시보드 통계 카드가 초기화됨") {
        Then("통계 카드 행(dash-stats-row)이 DOM에 존재한다") {
            page.querySelector(".dash-stats-row") shouldNotBe null
        }
        Then("통계 카드 3개가 렌더링된다") {
            val cards = page.querySelectorAll(".dash-stat-card")
            cards.count() shouldBe 3
        }
        Then("통계 값 요소도 정확히 3개이다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values.count() shouldBe 3
        }
        Then("각 통계 값은 비어있지 않고 숫자를 포함한다") {
            val values = page.querySelectorAll(".dash-stat-value")
            for (i in 0 until values.count()) {
                val text = values[i].textContent() ?: ""
                text.shouldNotBeBlank()
                text.contains(Regex("\\d")) shouldBe true
            }
        }
        Then("타입 수가 테스트 데이터 값(12)과 일치한다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values[0].textContent() shouldBe "12"
        }
        Then("문서 수가 테스트 데이터 값(1245)과 일치한다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values[1].textContent() shouldBe "1245"
        }
        Then("사용자 수가 테스트 데이터 값(8)과 일치한다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values[2].textContent() shouldBe "8"
        }
        Then("각 카드에 라벨(dash-stat-label) 요소가 존재한다") {
            val labels = page.querySelectorAll(".dash-stat-label")
            labels.count() shouldBe 3
        }
        Then("각 카드 라벨 텍스트가 비어있지 않다") {
            val labels = page.querySelectorAll(".dash-stat-label")
            for (i in 0 until labels.count()) {
                val text = labels[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
    }
})
