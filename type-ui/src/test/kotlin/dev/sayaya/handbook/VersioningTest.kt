package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlin.math.abs

/**
 * UC-T27: 타입 새 버전 생성 (Schema Evolution)
 * UC-T28: 타입 유효기간 편집 (Date Correction)
 */
@GwtHtml("canvastest.html")
internal class VersioningTest: GwtTestSpec({
     Given("타입 편집기 초기화됨") {
        page.onConsoleMessage { msg -> println("[BROWSER] ${msg.text()}") }
        page.setViewportSize(1280, 720)
        
        page.addInitScript("""
            (function() {
                window.__diag_logs = [];
                const originalLog = console.log;
                const originalError = console.error;
                console.log = function() {
                    window.__diag_logs.push(Array.from(arguments).join(' '));
                    originalLog.apply(console, arguments);
                };
                console.error = function() {
                    window.__diag_logs.push(Array.from(arguments).join(' '));
                    originalError.apply(console, arguments);
                };
            })()
        """.trimIndent())
        
        page.reload()
        page.waitForSelector(".type-box[data-type-key='customer:1.0']")
        Thread.sleep(1500)

        When("타입 박스(customer:1.0)를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            
            Then("상세 정보(TypeInspectorPanel)가 우측 패널에 노출되고, 툴바(TypeFloatingToolbar)가 표시된다") {
                page.waitForFunction("() => document.querySelector('.type-inspector-panel').classList.contains('visible')")
                page.waitForFunction("() => document.querySelector('.type-floating-toolbar').classList.contains('visible')")
            }
            
            Then("인스펙터에 해당 타입의 ID, 버전, 유효기간이 올바르게 표시된다") {
                page.waitForSelector(".type-inspector-panel .type-property-id:has-text('customer')")
                page.waitForSelector(".type-inspector-panel .type-property-version:has-text('1.0')")
            }
        }
        
        When("유효기간 라벨(.type-property-dates)을 클릭하면") {
            page.reload()
            page.waitForSelector(".type-box[data-type-key='customer:1.0']")
            page.click(".type-box[data-type-key='customer:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            Thread.sleep(500)

            page.click(".type-inspector-panel .type-property-dates", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            
            And("시작 날짜를 변경하고 Apply를 누르면") {
                page.waitForTimeout(500.0)
                page.evaluate("""
                    (function() {
                        const el = document.querySelector('#date-correction-start');
                        if(el) {
                            el.value = '2024-05-28';
                            el.dispatchEvent(new Event('input', {bubbles:true}));
                        }
                    })()
                """.trimIndent())
                page.click("#date-correction-apply")
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                Then("타입 속성 바의 날짜 텍스트가 갱신된다") {
                    page.waitForSelector(".type-inspector-panel .type-property-dates:has-text('2024-05-28')")
                }
            }
        }

        When("새 버전 생성 버튼(.type-ctrl-btn-new-version)을 클릭하면") {
            page.reload()
            page.waitForSelector(".type-box[data-type-key='customer:1.0']")
            val boxSelector = ".type-box[data-type-key='customer:1.0']"
            val box = page.locator(boxSelector)
            box.waitFor()
            val beforePos = box.boundingBox()!!
            
            page.click("$boxSelector .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            Thread.sleep(500)
            page.click(".type-ctrl-btn-new-version", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            
            And("개시 일시를 입력하고 'Create'를 누르면") {
                page.waitForTimeout(500.0)
                page.evaluate("""
                    (function() {
                        const elEffect = document.querySelector('#version-creation-effect');
                        const elVersion = document.querySelector('#version-creation-version');
                        if(elEffect && elVersion) {
                            elEffect.value = '2026-11-01';
                            elVersion.value = '2.0-final';
                            elEffect.dispatchEvent(new Event('input', {bubbles:true}));
                            elVersion.dispatchEvent(new Event('input', {bubbles:true}));
                        }
                    })()
                """.trimIndent())
                page.click("#version-creation-submit")
                page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                Then("새 버전의 타입이 기존 레이아웃 좌표를 상속받고, 이전 버전은 화면에서 사라진다") {
                    val newBoxSelector = ".type-box[data-type-key*='2.0-final']"
                    val newBox = page.locator(newBoxSelector)
                    
                    try {
                        newBox.waitFor(com.microsoft.playwright.Locator.WaitForOptions().setTimeout(10000.0))
                    } catch (e: Exception) {
                        val diag = page.evaluate("""
                            (function() {
                                return {
                                    logs: window.__diag_logs,
                                    boxes: Array.from(document.querySelectorAll('.type-box')).map(b => b.getAttribute('data-type-key'))
                                };
                            })()
                        """.trimIndent())
                        throw AssertionError("새 버전 렌더링 실패. Logs: $diag", e)
                    }
                    
                    val afterPos = newBox.boundingBox()!!
                    abs(afterPos.x - beforePos.x) shouldBeLessThan 5.0
                    abs(afterPos.y - beforePos.y) shouldBeLessThan 5.0

                    // 이전 버전은 새 레이아웃(현재 뷰)에서 보이지 않아야 함
                    page.locator(boxSelector).count() shouldBe 0
                }
            }
        }
        
        When("모바일 해상도(375x812)로 변경하고 타입을 선택하면") {
            page.setViewportSize(375, 812)
            page.reload()
            page.waitForSelector(".type-box[data-type-key='customer:1.0']")
            Thread.sleep(1000)
            
            page.click(".type-box[data-type-key='customer:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            
            Then("우측 패널과 툴바는 숨겨지고 바텀 시트(TypeBottomSheet)가 표시된다") {
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-inspector-panel')).display === 'none'")
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-floating-toolbar')).display === 'none'")
                page.waitForFunction("() => document.querySelector('.type-bottom-sheet').classList.contains('visible')")
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-bottom-sheet')).display === 'flex'")
                
                // 모바일 바텀 시트의 내용 확인
                page.waitForSelector(".type-bottom-sheet .type-property-id:has-text('customer')")
            }
        }
    }
})
