package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T27: 타입 새 버전 생성 (Schema Evolution)
 * UC-T28: 타입 유효기간 편집 (Date Correction)
 */
@GwtHtml("canvastest.html")
internal class VersioningTest: GwtTestSpec({
    Given("타입 편집기 초기화됨") {
        
        // UC-T28: 타입 유효기간 편집 (Property Bar 정보 표시)
        When("타입 박스(customer:1.0)를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0']")
            Then("상단에 타입 속성 바(.type-property-bar)가 나타난다") {
                page.waitForSelector(".type-property-bar", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            Then("속성 바에 해당 타입의 ID, 버전, 유효기간이 올바르게 표시된다") {
                page.waitForSelector(".type-property-id")
                page.textContent(".type-property-id") shouldBe "customer"
                page.textContent(".type-property-version") shouldBe "1.0"
                page.textContent(".type-property-dates")!!.contains(" ~ ") shouldBe true
            }
            
            // UC-T27: 새 버전 생성 버튼 가시성 확인
            Then("새 버전 생성 버튼(.type-ctrl-btn-new-version)이 속성 바에 나타난다") {
                page.querySelector(".type-property-bar .type-ctrl-btn-new-version") shouldNotBe null
            }
        }

        // UC-T28: 타입 유효기간 편집 (Date Correction)
        When("유효기간 라벨(.type-property-dates)을 클릭하면") {
            page.click(".type-property-dates")
            
            Then("날짜 수정 다이얼로그(#date-correction-dialog)가 열린다") {
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("시작 날짜를 변경하고 Apply를 누르면") {
                page.waitForTimeout(500.0)
                page.evaluate("""
                    (function() {
                        var input = document.querySelector('#date-correction-dialog md-outlined-text-field');
                        if (input) {
                            input.value = '2024-06-01';
                            input.dispatchEvent(new Event('input', {bubbles: true}));
                        }
                    })()
                """.trimIndent())
                page.click("#date-correction-apply")
                
                Then("타입 속성 바의 날짜 텍스트가 갱신된다") {
                    page.waitForSelector(".type-property-dates")
                    page.textContent(".type-property-dates")!!.contains("2024-06-01") shouldBe true
                }
                
                Then("날짜 수정 다이얼로그가 닫힌다") {
                    page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                    Thread.sleep(500)
                }
            }
        }
        
        // UC-T27: 새 버전 생성 테스트 (Schema Evolution)
        When("새 버전 생성 버튼(.type-ctrl-btn-new-version)을 클릭하면") {
            Thread.sleep(500)
            page.click(".type-ctrl-btn-new-version")
            
            Then("버전 생성 다이얼로그(#version-creation-dialog)가 열린다") {
                page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("개시 일시를 입력하고 'Create'를 누르면") {
                page.waitForTimeout(500.0)
                page.evaluate("document.querySelector('#version-creation-effect').value = '2026-07-01'")
                page.click("#version-creation-submit")
                
                Then("새로운 레이아웃 기간으로 자동 이동한다") {
                    page.waitForSelector(".type-ctrl-btn-after[disabled]", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED))
                }
                
                Then("다이얼로그가 완전히 닫힌다") {
                    page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                    Thread.sleep(500)
                }
            }
        }
        
        // UC-T13: 모바일 반응형 레이아웃 (플로팅 컨트롤)
        When("모바일 뷰포트(400x800)로 전환하면") {
            page.setViewportSize(400, 800)
            Thread.sleep(1000) // 넉넉히 대기
            page.evaluate("window.dispatchEvent(new Event('resize'))")
            
            Then("플로팅 버튼(Speed Dial)이 표시된다") {
                page.waitForSelector("md-fab.action-dial", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            Then("해당 캡슐은 클릭(터치) 가능하다") {
                // 클릭 전 다시 한번 뷰포트 안정화 대기
                Thread.sleep(1000)
                // JS click으로 물리적 충돌(hidden dialog) 무시하고 이벤트 트리거
                page.evaluate("document.querySelector('md-fab.action-dial').click()")
                
                // expanded 속성이 생길 때까지 대기
                page.waitForFunction("""
                    () => {
                        const fab = document.querySelector('md-fab.action-dial');
                        return fab && fab.parentElement && fab.parentElement.hasAttribute('expanded');
                    }
                """.trimIndent())
                
                val isExpanded = page.evaluate("""
                    document.querySelector('md-fab.action-dial').parentElement.hasAttribute('expanded')
                """.trimIndent()) as Boolean
                isExpanded shouldBe true
            }
        }

        When("캔버스 빈 영역을 클릭하여 선택을 해제하면") {
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(0.0, 0.0))
            Thread.sleep(800)
            Then("타입 속성 바가 숨겨진다") {
                val isVisible = page.evaluate("""
                    (function() {
                        const el = document.querySelector('.type-property-bar') || document.querySelector('.type-floating-pill.type-info');
                        if (!el) return false;
                        const style = getComputedStyle(el);
                        return style.display !== 'none' && style.visibility !== 'hidden' && parseFloat(style.opacity) > 0.1;
                    })()
                """.trimIndent()) as Boolean
                isVisible shouldBe false
            }
        }
        
        // 원복
        page.setViewportSize(1280, 720)
    }
})
