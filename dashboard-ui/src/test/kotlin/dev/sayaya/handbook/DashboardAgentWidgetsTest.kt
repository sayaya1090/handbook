package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("dashboardtest.html")
internal class DashboardAgentWidgetsTest: GwtTestSpec({
    Given("대시보드 에이전트 위젯이 초기화됨") {
        // === ActiveExecutionsWidget ===
        Then("활성 실행 위젯(dash-active-executions)이 DOM에 존재한다") {
            page.querySelector(".dash-active-executions") shouldNotBe null
        }
        Then("활성 실행 위젯 헤더 텍스트가 비어있지 않다") {
            val header = page.querySelector(".dash-active-executions .dash-panel-header")
            header shouldNotBe null
            val text = header!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("활성 실행 목록 컨테이너(dash-executions-list)가 존재한다") {
            page.querySelector(".dash-executions-list") shouldNotBe null
        }
        Then("활성 실행 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-exec-row")
            rows.count() shouldBe 2
        }
        Then("각 실행의 의도(dash-exec-intent) 텍스트가 비어있지 않다") {
            val intents = page.querySelectorAll(".dash-exec-intent")
            intents.count() shouldBe 2
            for (i in 0 until intents.count()) {
                val text = intents[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("각 실행에 진행률 바(dash-exec-progress-bar)가 존재한다") {
            val bars = page.querySelectorAll(".dash-exec-progress-bar")
            bars.count() shouldBe 2
        }
        Then("각 진행률 바 내부의 fill 요소 너비가 0보다 크다") {
            val fills = page.querySelectorAll(".dash-exec-progress-fill")
            fills.count() shouldBe 2
            for (i in 0 until fills.count()) {
                val width = fills[i].evaluate("el => el.style.width") as String
                width.shouldNotBeBlank()
                width.contains(Regex("\\d")) shouldBe true
            }
        }
        Then("진행률 텍스트(dash-exec-progress-text)에 슬래시 형식이 표시된다") {
            val texts = page.querySelectorAll(".dash-exec-progress-text")
            texts.count() shouldBe 2
            for (i in 0 until texts.count()) {
                val text = texts[i].textContent() ?: ""
                text.shouldNotBeBlank()
                text.shouldContain("/")
            }
        }
        Then("각 실행의 상태 배지(dash-exec-status) 텍스트가 비어있지 않다") {
            val statuses = page.querySelectorAll(".dash-exec-status")
            statuses.count() shouldBe 2
            for (i in 0 until statuses.count()) {
                val text = statuses[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("실행 상태에 RUNNING이 포함된다") {
            val status = page.querySelector(".dash-exec-status")
            status shouldNotBe null
            status!!.textContent() shouldBe "RUNNING"
        }

        // === ArtifactListWidget ===
        Then("아티팩트 목록 위젯(dash-artifact-list)이 DOM에 존재한다") {
            page.querySelector(".dash-artifact-list") shouldNotBe null
        }
        Then("아티팩트 위젯 헤더 텍스트가 비어있지 않다") {
            val header = page.querySelector(".dash-artifact-list .dash-panel-header")
            header shouldNotBe null
            val text = header!!.textContent() ?: ""
            text.shouldNotBeBlank()
        }
        Then("아티팩트 항목 컨테이너(dash-artifact-items)가 존재한다") {
            page.querySelector(".dash-artifact-items") shouldNotBe null
        }
        Then("아티팩트 2건이 렌더링된다") {
            val rows = page.querySelectorAll(".dash-artifact-row")
            rows.count() shouldBe 2
        }
        Then("각 아티팩트의 요약(dash-artifact-summary) 텍스트가 비어있지 않다") {
            val summaries = page.querySelectorAll(".dash-artifact-summary")
            summaries.count() shouldBe 2
            for (i in 0 until summaries.count()) {
                val text = summaries[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("각 아티팩트의 변경 수(dash-artifact-changes)에 숫자가 포함된다") {
            val changes = page.querySelectorAll(".dash-artifact-changes")
            changes.count() shouldBe 2
            for (i in 0 until changes.count()) {
                val text = changes[i].textContent() ?: ""
                text.shouldNotBeBlank()
                text.contains(Regex("\\d")) shouldBe true
            }
        }
        Then("첫 번째 아티팩트 변경 수는 2이다") {
            val change = page.querySelector(".dash-artifact-row:first-child .dash-artifact-changes")
            change shouldNotBe null
            val text = change!!.textContent() ?: ""
            text.shouldContain("2")
        }
        Then("각 아티팩트의 시간(dash-artifact-time) 텍스트가 비어있지 않다") {
            val times = page.querySelectorAll(".dash-artifact-time")
            times.count() shouldBe 2
            for (i in 0 until times.count()) {
                val text = times[i].textContent() ?: ""
                text.shouldNotBeBlank()
            }
        }
        Then("아티팩트의 상세(dash-artifact-details)가 초기에 숨겨져 있다") {
            val details = page.querySelectorAll(".dash-artifact-details")
            details.count() shouldBe 2
            for (i in 0 until details.count()) {
                val display = details[i].evaluate("el => el.style.display") as String
                display shouldBe "none"
            }
        }

        When("첫 번째 아티팩트 행을 클릭하면") {
            page.click(".dash-artifact-row:first-child")
            Thread.sleep(300)
            Then("상세가 펼쳐진다 (display가 block)") {
                val detail = page.querySelector(".dash-artifact-row:first-child .dash-artifact-details")
                detail shouldNotBe null
                val display = detail!!.evaluate("el => el.style.display") as String
                display shouldBe "block"
            }
            Then("펼쳐진 행에 dash-artifact-row-expanded 클래스가 추가된다") {
                val row = page.querySelector(".dash-artifact-row:first-child")
                val hasClass = row!!.evaluate("el => el.classList.contains('dash-artifact-row-expanded')") as Boolean
                hasClass shouldBe true
            }
            Then("상세에 변경 라인(dash-artifact-change-line)이 존재한다") {
                val lines = page.querySelectorAll(".dash-artifact-row:first-child .dash-artifact-change-line")
                lines.count() shouldBe 2
            }
        }

        When("펼쳐진 첫 번째 아티팩트 행을 다시 클릭하면") {
            page.click(".dash-artifact-row:first-child")
            Thread.sleep(300)
            Then("상세가 접힌다 (display가 none)") {
                val detail = page.querySelector(".dash-artifact-row:first-child .dash-artifact-details")
                val display = detail!!.evaluate("el => el.style.display") as String
                display shouldBe "none"
            }
            Then("dash-artifact-row-expanded 클래스가 제거된다") {
                val row = page.querySelector(".dash-artifact-row:first-child")
                val hasClass = row!!.evaluate("el => el.classList.contains('dash-artifact-row-expanded')") as Boolean
                hasClass shouldBe false
            }
        }
    }
})
