package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("src/test/webapp/dashboardtest.html")
internal class DashboardQualityTest: GwtTestSpec({
    Given("대시보드 품질 패널이 초기화됨") {
        Then("품질 패널(dash-quality-panel)이 DOM에 존재한다") {
            page.querySelector(".dash-quality-panel") shouldNotBe null
        }
        Then("품질 패널 헤더 텍스트가 비어있지 않다") {
            val header = page.querySelector(".dash-quality-panel .dash-panel-header")
            header shouldNotBe null
            val text = header!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("품질 이슈 목록 컨테이너(dash-quality-list)가 존재한다") {
            page.querySelector(".dash-quality-list") shouldNotBe null
        }
        Then("품질 이슈 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-quality-row")
            rows.count() shouldBe 2
        }
        Then("각 이슈 행에 severity 배지가 존재하고 텍스트가 있다") {
            val badges = page.querySelectorAll(".dash-severity-badge")
            badges.count() shouldBe 2
            for (i in 0 until badges.count()) {
                val text = badges[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("첫 번째 배지는 error severity이다") {
            val badge = page.querySelector(".dash-quality-row:first-child .dash-severity-badge")
            badge shouldNotBe null
            badge!!.textContent() shouldBe "error"
        }
        Then("두 번째 배지는 warning severity이다") {
            val badge = page.querySelector(".dash-quality-row:last-child .dash-severity-badge")
            badge shouldNotBe null
            badge!!.textContent() shouldBe "warning"
        }
        Then("각 이슈의 메시지(dash-quality-message)가 비어있지 않다") {
            val msgs = page.querySelectorAll(".dash-quality-message")
            msgs.count() shouldBe 2
            for (i in 0 until msgs.count()) {
                val text = msgs[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("첫 번째 이슈 메시지에 타입과 시리얼이 포함된다") {
            val msg = page.querySelector(".dash-quality-row:first-child .dash-quality-message")
            msg shouldNotBe null
            val text = msg!!.textContent() ?: ""
            text.shouldContain("customer")
            text.shouldContain("C-001")
        }
        Then("severity 배지에 dash-severity-error CSS 클래스가 적용된다") {
            val badge = page.querySelector(".dash-severity-error")
            badge shouldNotBe null
        }
        Then("severity 배지에 dash-severity-warning CSS 클래스가 적용된다") {
            val badge = page.querySelector(".dash-severity-warning")
            badge shouldNotBe null
        }
    }
})
