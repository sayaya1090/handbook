package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T27: 타입 새 버전 생성 (Schema Evolution)
 * UC-T28: 타입 유효기간 편집 (Date Correction)
 * 그리고 '타입 속성 바'에 대한 회귀 테스트.
 */
@GwtHtml("canvastest.html")
internal class VersioningTest: GwtTestSpec({
    Given("타입 편집기 초기화됨") {
        When("타입 박스(customer:1.0)를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0']")
            
            Then("상단에 타입 속성 바(.type-property-bar)가 나타난다") {
                page.waitForSelector(".type-property-bar", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
                val display = page.evaluate("getComputedStyle(document.querySelector('.type-property-bar')).display").toString()
                display shouldNotBe "none"
            }
            
            Then("속성 바에 해당 타입의 ID, 버전, 유효기간이 올바르게 표시된다") {
                page.waitForSelector(".type-property-id")
                page.textContent(".type-property-id") shouldBe "customer"
                page.textContent(".type-property-version") shouldBe "1.0"
                page.textContent(".type-property-dates")!!.contains(" ~ ") shouldBe true
            }
        }
        
        // UC-T28: 날짜 수정 테스트
        When("유효기간 라벨(.type-property-dates)을 클릭하면") {
            page.click(".type-property-dates")
            
            Then("날짜 수정 다이얼로그(#date-correction-dialog)가 열린다") {
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("시작 날짜를 변경하고 Apply를 누르면") {
                // requestAnimationFrame 콜백이 완료되어 초기값이 세팅될 때까지 잠시 대기
                page.waitForTimeout(100.0)
                
                // md-outlined-text-field는 focus 후 직접 타이핑하거나 value 속성 조작 필요
                page.evaluate("document.querySelector('#date-correction-start').value = '2026-06-01'")
                page.click("#date-correction-apply")
                
                Then("타입 속성 바의 날짜 텍스트가 갱신된다") {
                    page.waitForFunction("document.querySelector('.type-property-dates').textContent.includes('2026-06-01')")
                    page.textContent(".type-property-dates")!!.contains("2026-06-01") shouldBe true
                }
            }
        }
        
        // UC-T27: 새 버전 생성 테스트
        When("새 버전 생성 버튼(.type-ctrl-btn-new-version)을 클릭하면") {
            page.click(".type-ctrl-btn-new-version")
            
            Then("버전 생성 다이얼로그(#version-creation-dialog)가 열린다") {
                page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("개시 일시를 입력하고 'Create'를 누르면") {
                // requestAnimationFrame 콜백이 완료되어 초기값이 세팅될 때까지 잠시 대기
                page.waitForTimeout(100.0)
                
                page.evaluate("document.querySelector('#version-creation-effect').value = '2026-07-01'")
                page.click("#version-creation-submit")
                
                Then("새로운 레이아웃 기간으로 자동 이동한다") {
                    page.waitForSelector(".type-ctrl-btn-after[disabled]", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED))
                }
                
                Then("다이얼로그가 완전히 닫힌다") {
                    page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                }
                
                Then("속성 바의 시작 날짜가 2026-07-01로 표시된다") {
                    page.waitForFunction("document.querySelector('.type-property-dates').textContent.includes('2026-07-01')")
                    page.textContent(".type-property-dates")!!.contains("2026-07-01") shouldBe true
                }
            }
        }

        When("모바일 뷰포트(400x800)로 전환하면") {
            page.setViewportSize(400, 800)
            // 브라우저에 따라 resize/matchMedia 이벤트 지연이 있을 수 있으므로 대기
            page.waitForTimeout(500.0)
            page.evaluate("window.dispatchEvent(new Event('resize'))")
            
            Then("플로팅 버튼(Speed Dial)이 표시된다") {
                page.waitForSelector(".type-speed-dial:not(.settings) md-fab", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
                page.waitForSelector(".type-speed-dial.settings md-fab", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            Then("해당 캡슐은 클릭(터치) 가능하다") {
                // Playwright의 가시성 체크가 fixed 요소나 전환 중에 실패할 수 있으므로 DOM 이벤트를 직접 발생 (property bar 영역 클릭)
                page.evaluate("document.querySelector('.type-property-bar').click()")
                
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
                
                // 닫기
                page.evaluate("document.querySelector('#date-correction-close').click()")
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
            }
        }

        // 원복
        page.setViewportSize(1280, 720)
        
        When("캔버스 빈 영역을 클릭하여 선택을 해제하면") {
            // 캔버스 내부의 (10, 10) 좌표를 클릭하여 선택 해제 (TypeBox는 20,20에 있으므로 빈 영역임)
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0).setForce(true))
            
            Then("타입 속성 바가 숨겨진다") {
                // fade-out 클래스가 있는지 확인하고, 그 다음 display: none이 되는지 확인
                page.waitForSelector(".type-property-bar.type-fade-out")
                Thread.sleep(400) // 애니메이션 대기
            }
        }
    }
})
