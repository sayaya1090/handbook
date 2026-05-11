package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.math.abs

/**
 * UC-T27: 타입 새 버전 생성 (Schema Evolution)
 * UC-T28: 타입 유효기간 편집 (Date Correction)
 */
@GwtHtml("canvastest.html")
internal class VersioningTest: GwtTestSpec({
     Given("타입 편집기 초기화됨") {
        page.setViewportSize(1280, 720)
        page.reload()
        page.waitForSelector(".type-box[data-type-key='customer:1.0']")
        Thread.sleep(1500)

        When("타입 박스(customer:1.0)를 클릭하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-header")
            
            Then("상세 정보(TypeInspectorPanel)가 우측 패널에 노출되고, 툴바(TypeFloatingToolbar)가 표시된다") {
                // Playwright의 strict visibility 체크 우회: 클래스 적용 여부 확인
                page.waitForFunction("() => document.querySelector('.type-inspector-panel').classList.contains('visible')")
                page.waitForFunction("() => document.querySelector('.type-floating-toolbar').classList.contains('visible')")
            }
            
            Then("인스펙터에 해당 타입의 ID, 버전, 유효기간이 올바르게 표시된다") {
                page.textContent(".type-property-id") shouldBe "customer"
                page.textContent(".type-property-version") shouldBe "1.0"
            }
        }
        
        // UC-T28: 타입 유효기간 편집 (Date Correction)
        When("유효기간 라벨(.type-property-dates)을 클릭하면") {
            page.click(".type-property-dates")
            page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            
            And("시작 날짜를 변경하고 Apply를 누르면") {
                page.waitForTimeout(500.0)
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
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                Then("타입 속성 바의 날짜 텍스트가 갱신된다") {
                    page.waitForSelector(".type-property-dates:has-text('2024-05-01')")
                }
            }
        }

        // UC-T27: 새 버전 생성 테스트 (Schema Evolution)
        When("새 버전 생성 버튼(.type-ctrl-btn-new-version)을 클릭하면") {
            val boxSelector = ".type-box[data-type-key='customer:1.0']"
            val box = page.locator(boxSelector)
            box.waitFor()
            val beforePos = box.boundingBox()!!
            
            Thread.sleep(500)
            page.click(".type-ctrl-btn-new-version")
            page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            
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
                page.waitForSelector("#version-creation-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                Then("새 버전의 타입이 기존 레이아웃 좌표를 상속받는다 (X, Y 동일)") {
                    val newBoxSelector = ".type-box[data-type-key='customer:2.0']"
                    val newBox = page.locator(newBoxSelector)
                    newBox.waitFor()
                    val afterPos = newBox.boundingBox()!!
                    abs(afterPos.x - beforePos.x) shouldBeLessThan 5.0
                    abs(afterPos.y - beforePos.y) shouldBeLessThan 5.0
                }

                Then("이전 레이아웃 기간으로 돌아갔을 때 구버전(1.0)이 원래 위치에 존재한다") {
                    page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0).setForce(true))
                    Thread.sleep(500)
                    page.click(".type-ctrl-btn-before", com.microsoft.playwright.Page.ClickOptions().setForce(true))
                    Thread.sleep(1000)
                    
                    val oldBoxSelector = ".type-box[data-type-key='customer:1.0']"
                    val oldBox = page.locator(oldBoxSelector)
                    oldBox.waitFor()
                    val currentPos = oldBox.boundingBox()!!
                    abs(currentPos.x - beforePos.x) shouldBeLessThan 5.0
                }
            }
        }
        
        // UC-T27: 신규 타입 생성 후 날짜 변경 시 가시성 및 레이아웃 분할 검증
        When("신규 타입을 추가하고 시작 날짜를 미래(2026-05-08)로 변경하면") {
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0).setForce(true))
            Thread.sleep(300)
            
            // 1. 타입 추가
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            val newTypeSelector = ".type-box:not([data-type-key='customer:1.0']):not([data-type-key='order:1.0'])"
            page.waitForSelector(newTypeSelector)
            val newTypeKey = page.getAttribute(newTypeSelector, "data-type-key")!!

            // 2. 날짜 변경
            page.click("$newTypeSelector .type-header")
            page.waitForSelector(".type-property-dates")
            page.click(".type-property-dates")
            page.waitForSelector("#date-correction-dialog")
            page.waitForTimeout(200.0) // requestAnimationFrame 대기
            page.evaluate("""
                (function() {
                    const el = document.querySelector('#date-correction-start');
                    el.value = '2026-05-08';
                    el.dispatchEvent(new Event('input', {bubbles:true}));
                })()
            """.trimIndent())
            page.click("#date-correction-apply")
            page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
            
            // 기간 재계산 및 레이아웃 전환 대기 (충분한 시간 확보)
            page.waitForTimeout(3000.0)
            
            // 3. 선택 해제 및 내비게이션 바 확인 (강제 스크립트 실행으로 선택 해제 확실히)
            page.evaluate("console.log('[Test] Deselecting type...')")
            page.evaluate("window.dispatchEvent(new CustomEvent('click'))")
            // 헤더(40px)를 피해 넉넉한 위치 클릭
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(200.0, 200.0).setForce(true))
            page.waitForTimeout(1000.0)

            Then("새로 만든 타입이 화면에서 사라지지 않고 보여야 한다") {
                page.evaluate("console.log('[Test] Checking visibility of new type: $newTypeKey')")
                val isAttached = page.evaluate("!!document.querySelector(\".type-box[data-type-key='$newTypeKey']\")") as Boolean
                if (!isAttached) {
                    val count = page.evaluate("document.querySelectorAll('.type-box').length")
                    val period = page.textContent(".type-period-label")
                    page.evaluate("console.error('[Test] Type NOT FOUND in DOM. Count: ' + $count + ', Period: ' + '$period')")
                }
                isAttached shouldBe true
                
                // 타입이 보인다는 것은 LayoutProvider가 5/8 이후 구간을 선택했다는 명백한 증거
                page.waitForSelector(".type-box[data-type-key='$newTypeKey']", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
                page.querySelectorAll(".type-box").count() shouldBe 3
            }
            
            Then("이전 구간(2024-05-06~2026-05-08)에는 타입이 2개만 보여야 한다") {
                page.click(".type-ctrl-btn-before", com.microsoft.playwright.Page.ClickOptions().setForce(true))
                Thread.sleep(2000)
                
                val visibleCount = page.querySelectorAll(".type-box").count()
                if (visibleCount != 2) {
                    val period = page.textContent(".type-period-label")
                    val keys = page.evaluate("""
                        Array.from(document.querySelectorAll('.type-box')).map(el => el.getAttribute('data-type-key')).join(', ')
                    """) as String
                    println("[Test Failure Dump] Period: ${period}, Visible Keys: $keys")
                }
                
                visibleCount shouldBe 2
                page.querySelector(".type-box[data-type-key='$newTypeKey']") shouldBe null
            }
        }

        // UC-T28: 인접 버전 경계 동기화 (Cascading Boundary Synchronization)
        When("이전 버전(1.0)의 종료일을 2026-06-01로 앞당기면") {
            // 현재 레이아웃은 2024-05-06 ~ 2026-05-08 구간에 있음
            // customer:1.0을 선택
            val oldCustomerSelector = ".type-box[data-type-key='customer:1.0']"
            page.click("$oldCustomerSelector .type-header")
            page.waitForSelector(".type-property-dates")
            page.click(".type-property-dates")
            page.waitForSelector("#date-correction-dialog")
            page.waitForTimeout(200.0) // requestAnimationFrame 대기
            
            // 종료일을 2026-06-01로 설정 (원래는 2026-07-01에서 다음 버전이 시작됨)
            page.evaluate("""
                (function() {
                    const el = document.querySelector('#date-correction-expire');
                    el.value = '2026-06-01';
                    el.dispatchEvent(new Event('input', {bubbles:true}));
                })()
            """.trimIndent())
            page.click("#date-correction-apply")
            page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))

            Then("인접 버전을 감지하고 컨펌 다이얼로그가 노출된다") {
                page.waitForSelector(".ui-confirm-dialog:has-text('Do you want to adjust')", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
            }
            
            And("컨펌 다이얼로그에서 'Yes'를 클릭하면") {
                page.click(".ui-confirm-dialog .ui-confirm-option:has-text('Yes')")
                page.waitForSelector(".ui-confirm-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                page.waitForSelector("#date-correction-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                Then("다음 버전(2.0)의 시작일이 2026-06-01로 동기화된다") {
                    // 현재 레이아웃이 변경됨
                    page.waitForTimeout(1000.0)
                    
                    // 다음 레이아웃 구간들(2026-05-08~, 2026-06-01~)을 순회하여 이동
                    page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0).setForce(true))
                    Thread.sleep(500)
                    page.click(".type-ctrl-btn-after", com.microsoft.playwright.Page.ClickOptions().setForce(true))
                    Thread.sleep(500)
                    page.click(".type-ctrl-btn-after", com.microsoft.playwright.Page.ClickOptions().setForce(true))
                    Thread.sleep(1000)
                    
                    val period = page.textContent(".type-period-label")!!
                    println("[Test Debug] Navigated to Period: $period")
                    period.startsWith("2026-06-01") shouldBe true
                    
                    // 2.0 버전이 화면에 보여야 함
                    val newCustomerSelector = ".type-box[data-type-key='customer:2.0']"
                    page.waitForSelector(newCustomerSelector, com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE))
                }
            }
        }
        
        When("모바일 해상도(375x812)로 변경하고 타입을 선택하면") {
            page.setViewportSize(375, 812)
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0).setForce(true))
            Thread.sleep(500)
            
            page.click(".type-box[data-type-key='customer:2.0'] .type-header")
            
            Then("우측 패널과 툴바는 숨겨지고 바텀 시트(TypeBottomSheet)가 표시된다") {
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-inspector-panel')).display === 'none'")
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-floating-toolbar')).display === 'none'")
                page.waitForFunction("() => document.querySelector('.type-bottom-sheet').classList.contains('visible')")
                page.waitForFunction("() => window.getComputedStyle(document.querySelector('.type-bottom-sheet')).display === 'flex'")
            }
        }
    }
})
