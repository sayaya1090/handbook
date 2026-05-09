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
        // 전회 테스트(CanvasTest 등)에서의 상태 오염 방지를 위해 페이지 새로고침
        page.reload()
        page.waitForSelector(".type-box[data-type-key='customer:1.0']")
        Thread.sleep(1500)

        When("타입 박스(customer:1.0)를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0']")
            
            Then("상단에 타입 속성 바(.type-property-bar)가 나타난다") {
                page.waitForSelector(".type-property-bar", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            Then("속성 바에 해당 타입의 ID, 버전, 유효기간이 올바르게 표시된다") {
                page.textContent(".type-property-id") shouldBe "customer"
                page.textContent(".type-property-version") shouldBe "1.0"
                page.querySelector(".type-property-dates") shouldNotBe null
            }

            Then("새 버전 생성 버튼(.type-ctrl-btn-new-version)이 속성 바에 나타난다") {
                page.querySelector(".type-ctrl-btn-new-version") shouldNotBe null
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
                // 가시성 유지를 위해 현재 레이아웃(2024-05-06) 이전인 2024-05-01로 설정
                page.evaluate("""
                    (function() {
                        const el = document.querySelector('#date-correction-start');
                        if(el) {
                            el.value = '2024-05-01';
                            el.dispatchEvent(new Event('input', {bubbles:true}));
                        }
                    })()
                """.trimIndent())
                page.click("#date-correction-apply")
                
                Then("타입 속성 바의 날짜 텍스트가 갱신된다") {
                    page.waitForSelector(".type-property-dates:has-text('2024-05-01')")
                }
                
                Then("날짜 수정 다이얼로그가 닫힌다") {
                    page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                }
            }
        }

        // UC-T27: 새 버전 생성 테스트 (Schema Evolution)
        When("새 버전 생성 버튼(.type-ctrl-btn-new-version)을 클릭하면") {
            val boxSelector = ".type-box[data-type-key='customer:1.0']"
            val box = page.locator(boxSelector)
            // 이전 테스트에서 날짜를 2024-05-01로 바꿨으므로 여전히 보여야 함
            box.waitFor()
            val beforePos = box.boundingBox()!!
            
            Thread.sleep(500)
            page.click(".type-ctrl-btn-new-version")
            
            Then("버전 생성 다이얼로그(#version-creation-dialog)가 열린다") {
                page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("개시 일시를 입력하고 'Create'를 누르면") {
                page.waitForTimeout(500.0)
                
                page.evaluate("""
                    (function() {
                        const elEffect = document.querySelector('#version-creation-effect');
                        const elVersion = document.querySelector('#version-creation-version');
                        if(elEffect && elVersion) {
                            elEffect.value = '2026-07-01';
                            elVersion.value = '2.0';
                            elEffect.dispatchEvent(new Event('input', {bubbles:true}));
                            elVersion.dispatchEvent(new Event('input', {bubbles:true}));
                        }
                    })()
                """.trimIndent())
                page.click("#version-creation-submit")
                
                Then("새로운 레이아웃 기간으로 자동 이동한다") {
                    page.waitForSelector(".type-ctrl-btn-after[disabled]", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED))
                }
                
                Then("다이얼로그가 완전히 닫힌다") {
                    page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                    Thread.sleep(500)
                }

                Then("새 버전의 타입이 기존 레이아웃 좌표를 상속받는다 (X, Y 동일)") {
                    val newBoxSelector = ".type-box[data-type-key='customer:2.0']"
                    val newBox = page.locator(newBoxSelector)
                    newBox.waitFor()
                    val afterPos = newBox.boundingBox()!!
                    
                    afterPos.x shouldBe beforePos.x
                    afterPos.y shouldBe beforePos.y
                }

                Then("이전 레이아웃 기간의 종료 일시가 새 버전의 시작 일시와 일치한다 (중첩 방지)") {
                    page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(0.0, 0.0))
                    Thread.sleep(500)
                    page.click(".type-ctrl-btn-before")
                    Thread.sleep(500)
                    
                    val periodText = page.textContent(".type-period-label")!!
                    periodText.endsWith("2026-07-01") shouldBe true
                }
            }
        }
        
        When("모바일 뷰포트(400x800)로 전환하면") {
            page.setViewportSize(400, 800)
            Thread.sleep(500)
            
            Then("플로팅 버튼(Speed Dial)이 표시된다") {
                page.waitForSelector(".type-speed-dial", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
        }
        
        When("캔버스 빈 영역을 클릭하여 선택을 해제하면") {
            page.setViewportSize(1280, 720)
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(0.0, 0.0))
            Thread.sleep(300)
            Then("타입 속성 바가 숨겨진다") {
                val isVisible = page.evaluate("""
                    (() => {
                        const el = document.querySelector('.type-property-bar');
                        if(!el) return false;
                        const style = window.getComputedStyle(el);
                        return style.display !== 'none' && style.visibility !== 'hidden' && parseFloat(style.opacity) > 0.1;
                    })()
                """.trimIndent()) as Boolean
                isVisible shouldBe false
            }
        }
        
        page.setViewportSize(1280, 720)
    }
})
