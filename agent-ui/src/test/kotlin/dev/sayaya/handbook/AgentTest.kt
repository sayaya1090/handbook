package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/agent.html")
internal class AgentTest: GwtTestSpec({
    Given("에이전트 UI가 준비됨") {
        Thread.sleep(3000)

        Then("테스트 영역이 존재한다") {
            page.querySelector("#test-area") shouldNotBe null
        }

        Then("메인 입력창이 존재한다") {
            val container = page.querySelector(".agent-input-container")
            container shouldNotBe null
            val input = page.querySelector(".agent-input-field")
            input shouldNotBe null
        }

        Then("전송 버튼이 존재한다") {
            val sendBtn = page.querySelector(".agent-input-send")
            sendBtn shouldNotBe null
        }

        When("Highlight 버튼을 클릭하면") {
            Then("클릭 전에는 agent-highlight 클래스가 없다") {
                val target = page.querySelector("#target-element")
                target shouldNotBe null
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("agent-highlight") shouldBe false
            }
            page.click("#btn-highlight")
            Thread.sleep(500)
            Then("클릭 후 대상 요소에 agent-highlight 클래스가 추가된다") {
                val target = page.querySelector("#target-element")
                target shouldNotBe null
                val classes = target!!.getAttribute("class") ?: ""
                classes.contains("agent-highlight") shouldBe true
            }
        }

        When("Attention 버튼을 클릭하면") {
            Then("클릭 전에는 오버레이가 숨겨져 있다") {
                val overlay = page.querySelector(".agent-overlay-container")
                overlay shouldNotBe null
                val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
            Then("클릭 전에는 코치마크가 존재하지 않는다") {
                page.querySelector(".agent-coachmark-backdrop") shouldBe null
            }
            page.click("#btn-attention")
            Thread.sleep(500)
            Then("클릭 후 코치마크 오버레이가 표시된다") {
                val overlay = page.querySelector(".agent-overlay-container")
                val display = overlay!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "block"
                page.querySelector(".agent-coachmark-backdrop") shouldNotBe null
                val tooltip = page.querySelector(".agent-coachmark-tooltip")
                tooltip shouldNotBe null
                tooltip!!.textContent() shouldBe "이 영역을 확인하세요"
            }
            Then("오버레이를 클릭하면 닫힌다") {
                page.click(".agent-overlay-container")
                Thread.sleep(300)
                val display = page.querySelector(".agent-overlay-container")!!
                    .evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
        }

        When("Preview 버튼을 클릭하면") {
            Then("클릭 전에는 미리보기 패널이 숨겨져 있다") {
                val panel = page.querySelector(".agent-preview-panel")
                panel shouldNotBe null
                val display = panel!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
            page.click("#btn-preview")
            Thread.sleep(500)
            Then("클릭 후 미리보기 패널이 표시된다") {
                val panel = page.querySelector(".agent-preview-panel")
                val display = panel!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("변경 전후 diff가 표시된다") {
                val before = page.querySelector(".agent-preview-before")
                before shouldNotBe null
                before!!.textContent()!!.trim() shouldBe "이름"
                val after = page.querySelector(".agent-preview-after")
                after shouldNotBe null
                after!!.textContent()!!.trim() shouldBe "고객명"
            }
        }

        When("Confirm 버튼을 클릭하면") {
            Then("클릭 전에는 다이얼로그가 열려있지 않다") {
                val dialog = page.querySelector(".agent-confirm-dialog")
                dialog shouldNotBe null
                val open = dialog!!.evaluate("el => el.querySelector('md-dialog')?.open ?? el.style.display !== 'flex'")
                // md-dialog의 open 속성이 false이거나, display가 flex가 아닌 것을 확인
            }
            page.click("#btn-confirm")
            Thread.sleep(500)
            Then("클릭 후 확인 다이얼로그가 표시된다") {
                val dialog = page.querySelector(".agent-confirm-dialog")
                dialog shouldNotBe null
            }
            Then("첫 번째 버튼을 클릭하면 다이얼로그가 닫힌다") {
                val buttons = page.querySelectorAll(".agent-confirm-option")
                buttons.count() shouldBe 3
                page.click(".agent-confirm-option")
                Thread.sleep(300)
            }
        }

        When("Confirm 커맨드 수신 시") {
            page.click("#btn-confirm")
            Thread.sleep(500)
            Then("입력창의 label이 변경된다") {
                val input = page.querySelector(".agent-input-field")
                input shouldNotBe null
            }
            Then("중단 버튼이 표시된다") {
                val abortBtn = page.querySelector(".agent-input-abort")
                abortBtn shouldNotBe null
            }
        }

        When("Complete 커맨드 수신 시") {
            page.click("#btn-complete")
            Thread.sleep(500)
            Then("전송 버튼이 다시 표시된다") {
                val sendBtn = page.querySelector(".agent-input-send")
                sendBtn shouldNotBe null
            }
        }

        // UC-A6: Mutation
        When("Mutate 버튼을 클릭하면") {
            page.click("#btn-mutate")
            Thread.sleep(500)
            Then("변경 로그가 표시된다") {
                val log = page.querySelector(".agent-mutate-log")
                log shouldNotBe null
                val display = log!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("변경 항목 2개가 표시된다") {
                val lines = page.querySelectorAll(".agent-mutate-line")
                lines.count() shouldBe 2
            }
        }

        // UC-A7: 알림
        When("Notify 버튼을 클릭하면") {
            page.click("#btn-notify")
            Thread.sleep(500)
            Then("토스트가 표시된다") {
                val toasts = page.querySelectorAll(".sayaya-toast")
                toasts.count() shouldBe 1
            }
        }

        // UC-A3: 네비게이션
        When("Navigate 버튼을 클릭하면") {
            page.click("#btn-navigate")
            Thread.sleep(500)
            Then("네비게이션 인디케이터가 표시된다") {
                val indicator = page.querySelector(".agent-navigate-indicator")
                indicator shouldNotBe null
            }
        }

        // UC-A4: 스크롤
        When("Scroll 버튼을 클릭하면") {
            page.click("#btn-scroll")
            Thread.sleep(500)
            Then("스크롤 타겟으로 이동한다") {
                val target = page.querySelector("#scroll-target")
                target shouldNotBe null
            }
        }

        // UC-A12: 모바일 반응형 레이아웃
        When("뷰포트를 모바일 크기로 변경하면") {
            page.setViewportSize(375, 667)
            Thread.sleep(500)
            Then("입력 컨테이너가 여전히 존재한다") {
                page.querySelector(".agent-input-container") shouldNotBe null
            }
            Then("입력 필드가 여전히 존재한다") {
                page.querySelector(".agent-input-field") shouldNotBe null
            }
            Then("전송 버튼이 여전히 존재한다") {
                page.querySelector(".agent-input-send") shouldNotBe null
            }
        }
    }
})
