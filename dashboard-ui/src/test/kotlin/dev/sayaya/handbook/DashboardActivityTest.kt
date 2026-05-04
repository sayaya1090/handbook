package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

/**
 * UC-DB3: 에이전트 활동 타임라인
 */
@GwtHtml("dashboardtest.html")
internal class DashboardActivityTest: GwtTestSpec({
    Given("대시보드 에이전트 활동 로그가 초기화됨") {
        Then("에이전트 활동 패널(dash-activity-panel)이 DOM에 존재한다") {
            page.querySelector(".dash-activity-panel") shouldNotBe null
        }
        Then("활동 패널 헤더 텍스트가 비어있지 않다") {
            val header = page.querySelector(".dash-activity-panel .dash-panel-header")
            header shouldNotBe null
            val text = header!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("활동 목록 컨테이너(dash-activity-list)가 존재한다") {
            page.querySelector(".dash-activity-list") shouldNotBe null
        }
        Then("에이전트 활동 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-activity-row")
            rows.count() shouldBe 2
        }
        Then("각 활동의 타임스탬프(dash-activity-time)가 HH:MM 형식이다") {
            val times = page.querySelectorAll(".dash-activity-time")
            times.count() shouldBe 2
            for (i in 0 until times.count()) {
                val text = times[i].textContent() ?: ""
                text.shouldNotBeBlank()
                text.contains(Regex("\\d{2}:\\d{2}")) shouldBe true
            }
        }
        Then("각 활동의 상태(dash-activity-status) 텍스트가 비어있지 않다") {
            val statuses = page.querySelectorAll(".dash-activity-status")
            statuses.count() shouldBe 2
            for (i in 0 until statuses.count()) {
                val text = statuses[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("활동 상태에 COMPLETE가 포함된다") {
            val status = page.querySelector(".dash-activity-status")
            status shouldNotBe null
            val text = status!!.textContent() ?: ""
            text.shouldContain("COMPLETE")
        }
        Then("각 활동의 의도(dash-activity-intent) 텍스트가 비어있지 않다") {
            val intents = page.querySelectorAll(".dash-activity-intent")
            intents.count() shouldBe 2
            for (i in 0 until intents.count()) {
                val text = intents[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("첫 번째 활동의 의도에 커맨드 수가 포함된다") {
            val intent = page.querySelector(".dash-activity-row:first-child .dash-activity-intent")
            intent shouldNotBe null
            val text = intent!!.textContent() ?: ""
            text.shouldContain("3")
        }
        Then("활동 상태에 dash-status-complete CSS 클래스가 적용된다") {
            val status = page.querySelector(".dash-status-complete")
            status shouldNotBe null
        }
    }
})
