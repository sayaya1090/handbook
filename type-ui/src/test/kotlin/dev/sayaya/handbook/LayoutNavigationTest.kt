package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 레이아웃 탐색 및 데이터 로딩 검증 테스트.
 * Type-UI가 레이아웃 기간을 이동할 때 서버에서 추가 데이터를 올바르게 로드하는지 확인한다.
 */
@GwtHtml("navigationtest.html")
internal class LayoutNavigationTest: GwtTestSpec({
    val infinity = 253402214400000.0
    val pastStart = 1000.0
    val pastEnd = 2000.0
    val currentStart = 2000.0
    val currentEnd = infinity

    Given("두 개의 레이아웃 기간과 각각의 타입이 Mock으로 설정됨") {
        page.setViewportSize(1280, 720)
        
        // Mock 데이터 주입 로직 (모든 페이지 로드 시 실행)
        page.addInitScript("""
            (function() {
                const infinity = 253402214400000.0;
                const pastStart = 1000.0;
                const pastEnd = 2000.0;
                const currentStart = 2000.0;
                const currentEnd = infinity;

                window.__mock_layouts = [
                    { id: "l0", workspace: "demo", effect_date_time: pastStart, expire_date_time: pastEnd, positions: { "type-past:1.0": { x: 100, y: 100, width: 200, height: 150 } } },
                    { id: "l1", workspace: "demo", effect_date_time: currentStart, expire_date_time: currentEnd, positions: { "type-current:1.0": { x: 200, y: 200, width: 200, height: 150 } } }
                ];

                window.__mock_types = {
                    [pastStart + ":" + pastEnd]: [
                        { id: "type-past", version: "1.0", effect_date_time: pastStart, expire_date_time: infinity, attributes: [] }
                    ],
                    [currentStart + ":" + currentEnd]: [
                        { id: "type-current", version: "1.0", effect_date_time: currentStart, expire_date_time: infinity, attributes: [] }
                    ]
                };

                window.__mock_positions = {
                    [pastStart + ":" + pastEnd]: {
                        "type-past:1.0": { x: 100, y: 100, width: 200, height: 150 }
                    },
                    [currentStart + ":" + currentEnd]: {
                        "type-current:1.0": { x: 200, y: 200, width: 200, height: 150 }
                    }
                };
            })()
        """.trimIndent())

        When("애플리케이션이 로드되고 워크스페이스 이벤트가 발생하면") {
            page.reload()
            page.evaluate("""
                (function() {
                    var detail = { workspaceId: 'demo' };
                    window.dispatchEvent(new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false}));
                })()
            """.trimIndent())

            Then("가장 최신 레이아웃(l1)이 자동 선택되고 해당 타입(type-current)이 표시된다") {
                page.waitForSelector(".type-box[data-type-key='type-current:1.0']")
                page.locator(".type-box[data-type-key='type-past:1.0']").count() shouldBe 0
                page.locator(".type-period-label").textContent() shouldBe "1970-01-01 ~ ∞"
            }
        }

        When("'Before' 버튼을 클릭하여 이전 기간(l0)으로 이동하면") {
            page.click(".type-ctrl-btn-before", com.microsoft.playwright.Page.ClickOptions().setForce(true))

            
            Then("레이아웃 기간 라벨이 변경된다") {
                // pastStart=1000.0, pastEnd=2000.0 -> "1970-01-01 ~ 1970-01-01"
                page.waitForSelector(".type-period-label:has-text('1970-01-01 ~ 1970-01-01')")
            }

            Then("이전 기간의 타입(type-past)이 서버에서 로드되어 화면에 표시되어야 한다") {
                // 이 부분에서 실패할 것으로 예상됨 (현재 로직은 추가 로딩을 하지 않음)
                page.waitForSelector(".type-box[data-type-key='type-past:1.0']", com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(2000.0))
                page.locator(".type-box[data-type-key='type-current:1.0']").count() shouldBe 0
            }
        }
    }
})
