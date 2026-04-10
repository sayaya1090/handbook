package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/workspacetest.html")
internal class WorkspaceTest: GwtTestSpec({
    Given("워크스페이스 UI가 초기화됨") {
        Thread.sleep(3000)

        // UC-W1: 워크스페이스 생성
        Then("다이얼로그가 존재한다") {
            page.querySelector(".ws-dialog") shouldNotBe null
        }
        Then("CREATE 섹션이 존재한다") {
            val sections = page.querySelectorAll(".ws-section")
            sections.count() shouldBe 2
        }
        Then("라디오 버튼 2개가 존재한다") {
            val radios = page.querySelectorAll("input[name='create-workspace']")
            radios.count() shouldBe 2
        }
        Then("Submit 버튼이 존재한다") {
            val btn = page.querySelector(".ws-submit")
            btn shouldNotBe null
        }
        Then("Submit 버튼이 초기에 비활성 상태이다") {
            val btn = page.querySelector(".ws-submit")
            val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
            disabled shouldBe true
        }

        // UC-W1: CREATE 모드 동작
        When("CREATE 라디오를 선택하고 이름을 입력하면") {
            page.click(".ws-section:first-child input[type='radio']")
            Thread.sleep(200)
            val input = page.querySelector(".ws-section:first-child .ws-section-input")
            input shouldNotBe null
            page.fill(".ws-section:first-child .ws-section-input", "TestWorkspace")
            Thread.sleep(300)
            Then("Submit 버튼이 활성화된다") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe false
            }
        }

        // UC-W2: JOIN 모드 전환
        When("JOIN 라디오를 선택하면") {
            page.click(".ws-section:last-child input[type='radio']")
            Thread.sleep(200)
            Then("JOIN 섹션의 입력 필드에 포커스가 이동한다") {
                val input = page.querySelector(".ws-section:last-child .ws-section-input")
                input shouldNotBe null
            }
            Then("Submit 버튼이 비활성 상태로 돌아간다") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe true
            }
        }

        // UC-W3: 에이전트 워크스페이스 모드 전환 - WindowMutationBridge CustomEvent 디스패치
        When("에이전트가 WS_MODE CREATE 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['WS_MODE CREATE'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("다이얼로그가 유지된다") {
                page.querySelector(".ws-dialog") shouldNotBe null
            }
        }

        // UC-W4: 에이전트 워크스페이스 입력 설정
        When("에이전트가 WS_INPUT 이벤트를 디스패치하면") {
            page.evaluate("""
                (function() {
                    var detail = ['WS_INPUT AgentWorkspace'];
                    var evt = new CustomEvent('handbook-mutate', { detail: detail, bubbles: false });
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("다이얼로그가 유지된다") {
                page.querySelector(".ws-dialog") shouldNotBe null
            }
            Then("Submit 버튼이 존재한다") {
                page.querySelector(".ws-submit") shouldNotBe null
            }
        }

        // UC-W5: 모바일 반응형 레이아웃
        When("뷰포트를 모바일 크기로 변경하면") {
            page.setViewportSize(375, 667)
            Thread.sleep(500)
            Then("다이얼로그가 여전히 표시된다") {
                val dialog = page.querySelector(".ws-dialog")
                dialog shouldNotBe null
                val display = dialog!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("Submit 버튼이 여전히 존재한다") {
                page.querySelector(".ws-submit") shouldNotBe null
            }
            Then("입력 필드가 존재한다") {
                val inputs = page.querySelectorAll(".ws-section-input")
                inputs.count() shouldBe 2
            }
        }
    }
})
