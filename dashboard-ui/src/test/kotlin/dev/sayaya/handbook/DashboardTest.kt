package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("src/test/webapp/dashboardtest.html")
internal class DashboardTest: GwtTestSpec({
    Given("대시보드 UI가 초기화됨") {
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
        Then("통계 값이 숫자로 표시된다") {
            val values = page.querySelectorAll(".dash-stat-value")
            values.count() shouldBe 3
            // 각 통계 값이 비어있지 않고 숫자를 포함해야 한다
            for (i in 0 until values.count()) {
                val text = values[i].textContent() ?: ""
                text.shouldNotBeBlank()
                text.contains(Regex("\\d")) shouldBe true
            }
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
        Then("severity 배지가 표시되고 텍스트가 있다") {
            val badge = page.querySelector(".dash-severity-badge")
            badge shouldNotBe null
            val text = badge!!.textContent() ?: ""
            text.shouldNotBeBlank()
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
        Then("활동 시간이 표시되고 텍스트가 있다") {
            val time = page.querySelector(".dash-activity-time")
            time shouldNotBe null
            val text = time!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("활동 상태가 표시되고 텍스트가 있다") {
            val status = page.querySelector(".dash-activity-status")
            status shouldNotBe null
            val text = status!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }

        // 활성 에이전트 실행 위젯 검증
        Then("활성 실행 위젯이 존재한다") {
            page.querySelector(".dash-active-executions") shouldNotBe null
        }
        Then("활성 실행 목록이 존재한다") {
            page.querySelector(".dash-executions-list") shouldNotBe null
        }
        Then("활성 실행 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-exec-row")
            rows.count() shouldBe 2
        }
        Then("실행 의도가 표시되고 텍스트가 있다") {
            val intent = page.querySelector(".dash-exec-intent")
            intent shouldNotBe null
            val text = intent!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("실행 진행률 바가 표시되고 너비가 0보다 크다") {
            val bar = page.querySelector(".dash-exec-progress-bar")
            bar shouldNotBe null
            val width = bar!!.evaluate("el => el.offsetWidth || el.getBoundingClientRect().width") as Number
            width.toInt() shouldBeGreaterThan 0
        }
        Then("실행 상태 배지가 표시되고 텍스트가 있다") {
            val status = page.querySelector(".dash-exec-status")
            status shouldNotBe null
            val text = status!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }

        // 아티팩트 목록 위젯 검증
        Then("아티팩트 목록 위젯이 존재한다") {
            page.querySelector(".dash-artifact-list") shouldNotBe null
        }
        Then("아티팩트 항목 목록이 존재한다") {
            page.querySelector(".dash-artifact-items") shouldNotBe null
        }
        Then("아티팩트 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-artifact-row")
            rows.count() shouldBe 2
        }
        Then("아티팩트 요약이 표시되고 텍스트가 있다") {
            val summary = page.querySelector(".dash-artifact-summary")
            summary shouldNotBe null
            val text = summary!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("아티팩트 변경 수가 숫자로 표시된다") {
            val changes = page.querySelector(".dash-artifact-changes")
            changes shouldNotBe null
            val text = changes!!.textContent() ?: ""
            text.shouldNotBeBlank()
            text.contains(Regex("\\d")) shouldBe true
        }
        Then("아티팩트 시간이 표시되고 텍스트가 있다") {
            val time = page.querySelector(".dash-artifact-time")
            time shouldNotBe null
            val text = time!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
    }
})
