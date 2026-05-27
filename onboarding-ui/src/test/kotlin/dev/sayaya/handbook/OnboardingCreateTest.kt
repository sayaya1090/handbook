package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-W1: 워크스페이스 생성
 * UC-W3: 에이전트에 의한 워크스페이스 생성
 * UC-W4: 에이전트 단계별 UI 조작
 * UC-W5: 모바일 반응형 레이아웃
 */
@GwtHtml("onboardingtest.html")
internal class OnboardingCreateTest: GwtTestSpec({
    Given("워크스페이스 CREATE UI가 초기화됨") {
        // UC-W1: 워크스페이스 생성 — 구조 검증
        Then("다이얼로그(.ws-dialog)가 DOM에 존재한다") {
            page.querySelector(".ws-dialog") shouldNotBe null
        }
        Then("섹션(.ws-section) 요소가 정확히 2개 존재한다") {
            val sections = page.querySelectorAll(".ws-section")
            sections.count() shouldBe 2
        }
        Then("라디오 버튼(md-radio)이 정확히 2개 존재한다") {
            val radios = page.querySelectorAll("md-radio")
            radios.count() shouldBe 2
        }
        Then("라디오 버튼이 같은 name 속성(create-workspace)을 공유한다") {
            val radios = page.querySelectorAll("md-radio[name='create-workspace']")
            radios.count() shouldBe 2
        }
        Then("입력 필드(md-outlined-text-field.ws-section-input)가 정확히 2개 존재한다") {
            val inputs = page.querySelectorAll("md-outlined-text-field.ws-section-input")
            inputs.count() shouldBe 2
        }
        Then("Submit 버튼(.ws-submit)이 존재한다") {
            val btn = page.querySelector(".ws-submit")
            btn shouldNotBe null
        }
        Then("Submit 버튼이 초기에 비활성(disabled) 상태이다") {
            val btn = page.querySelector(".ws-submit")
            val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
            disabled shouldBe true
        }

        // UC-W1: CREATE 모드 동작
        When("CREATE 라디오를 선택하고 이름을 입력하면") {
            val sections = page.querySelectorAll(".ws-section")
            val createSection = sections[0]
            createSection.querySelector("md-radio")!!.click()
            Thread.sleep(200)
            val input = createSection.querySelector("md-outlined-text-field.ws-section-input")
            input shouldNotBe null
            page.evaluate("document.querySelectorAll('.ws-section')[0].querySelector('md-outlined-text-field.ws-section-input').value = 'TestWorkspace'")
            page.evaluate("document.querySelectorAll('.ws-section')[0].querySelector('md-outlined-text-field.ws-section-input').dispatchEvent(new Event('input', {bubbles: true}))")
            Thread.sleep(300)
            Then("Submit 버튼이 활성화된다 (disabled=false)") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe false
            }
            Then("입력 필드에 입력한 값이 반영되어 있다") {
                val value = page.evaluate("document.querySelectorAll('.ws-section')[0].querySelector('md-outlined-text-field.ws-section-input').value") as String
                value shouldBe "TestWorkspace"
            }
            Then("Submit 버튼을 클릭하면 성공 응답(id)을 받고 UriSharing를 통해 /workspaces/{id}/dashboard로 네비게이션(navigate)을 호출한다") {
                page.evaluate("window.__handbook_uri_called = null; window.__handbook_uri = function(uri) { window.__handbook_uri_called = uri; }")
                page.querySelector(".ws-submit")?.click()
                
                // Wait for navigation call
                page.waitForFunction("window.__handbook_uri_called != null", null, com.microsoft.playwright.Page.WaitForFunctionOptions().setTimeout(2000.0))
                
                val uriCalled = page.evaluate("window.__handbook_uri_called")?.toString() ?: ""
                (uriCalled.contains("/workspaces/") && uriCalled.contains("/dashboard")) shouldBe true
            }
        }

        // UC-W3: 에이전트 워크스페이스 모드 전환
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
            Then("섹션 수가 유지된다") {
                val sections = page.querySelectorAll(".ws-section")
                sections.count() shouldBe 2
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
        When("뷰포트를 모바일 크기(375x667)로 변경하면") {
            page.setViewportSize(375, 667)
            Thread.sleep(500)
            Then("다이얼로그가 여전히 표시된다 (display != none)") {
                val dialog = page.querySelector(".ws-dialog")
                dialog shouldNotBe null
                val display = dialog!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldNotBe "none"
            }
            Then("Submit 버튼이 여전히 존재한다") {
                page.querySelector(".ws-submit") shouldNotBe null
            }
            Then("입력 필드 2개가 유지된다") {
                val inputs = page.querySelectorAll("md-outlined-text-field.ws-section-input")
                inputs.count() shouldBe 2
            }
        }
    }
})
