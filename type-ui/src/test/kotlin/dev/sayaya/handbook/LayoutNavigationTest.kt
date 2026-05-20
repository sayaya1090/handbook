package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec문서
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * 레이아웃 탐색 및 데이터 로딩 검증 테스트.
 * Type-UI가 레이아웃 기간을 이동할 때 서버에서 추가 데이터를 올바르게 로드하는지 확인한다.
 */
@GwtHtml("navigationtest.html")
internal class LayoutNavigationTest: GwtTestSpec({
    val infinity = 253402214400000.0

    Given("두 개의 레이아웃 기간 데이터 Mock 설정") {
        page.setViewportSize(1280, 720)
        
        // PeriodRecalculationService가 생성할 레이아웃과 일치하도록 설정
        val pStart: Long = 0
        val pEnd: Long = 2000
        val cStart: Long = 2000
        val cEnd: Long = 253402214400000L

        page.addInitScript("""
            (function() {
                window.__mock_layouts = [
                    { id: "l0", workspace: "demo", effect_date_time: $pStart, expire_date_time: $pEnd, positions: { "type-past:1.0": { x: 10, y: 10, width: 200, height: 150 } } },
                    { id: "l1", workspace: "demo", effect_date_time: $cStart, expire_date_time: $cEnd, positions: { "type-current:1.0": { x: 200, y: 200, width: 200, height: 150 } } }
                ];

                window.__mock_types = {
                    "0:2000": [
                        { id: "type-past", version: "1.0", effect_date_time: $pStart, expire_date_time: $cEnd, attributes: [] }
                    ],
                    "2000:$cEnd": [
                        { id: "type-current", version: "1.0", effect_date_time: $cStart, expire_date_time: $cEnd, attributes: [] }
                    ]
                };

                window.__mock_positions = {
                    "0:2000": { "type-past:1.0": { x: 10, y: 10, width: 200, height: 150 } },
                    "2000:$cEnd": { "type-current:1.0": { x: 200, y: 200, width: 200, height: 150 } }
                };
            })()
        """.trimIndent())

        page.reload()
        page.evaluate("""
            (function() {
                var detail = { workspaceId: 'demo' };
                window.dispatchEvent(new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false}));
            })()
        """.trimIndent())
        
        page.waitForSelector(".type-box[data-type-key='type-current:1.0']")

        When("애플리케이션이 로드되면") {
            Then("가장 최신 레이아웃(l1)이 자동 선택되고 해당 타입(type-current)이 표시된다") {
                page.locator(".type-box[data-type-key='type-current:1.0']").isVisible shouldBe true
                page.locator(".type-box[data-type-key='type-past:1.0']").count() shouldBe 0
            }
        }

        When("'Before' 버튼을 클릭하여 이전 기간(l0)으로 이동하면") {
            // 초기 로드 및 버튼 활성화 대기
            page.waitForSelector(".type-box[data-type-key='type-current:1.0']")
            page.waitForSelector(".type-ctrl-btn-before:not([disabled])")
            
            page.click(".type-ctrl-btn-before", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            
            Then("이전 기간의 타입(type-past)이 서버에서 로드되어 화면에 표시되어야 한다") {
                page.waitForSelector(".type-box[data-type-key='type-past:1.0']", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000.0))
                page.locator(".type-box[data-type-key='type-current:1.0']").count() shouldBe 0
            }
        }
    }
})
