package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * 타입 간 참조 정합성 및 지능형 자동 보정 기능 검증 테스트.
 */
@GwtHtml("navigationtest.html")
internal class ReferenceIntegrityTest: GwtTestSpec({
    val infinity = 253402214400000.0

    Given("참조 가능한 타입(B)이 미래 시점에만 존재하는 환경") {
        page.setViewportSize(1280, 720)
        
        page.addInitScript("""
            (function() {
                const infinity = 253402214400000.0;
                const futureStart = Date.now() + 86400000 * 10; // 10일 후

                window.__mock_layouts = [
                    { id: "l1", workspace: "demo", effect_date_time: 0, expire_date_time: infinity, positions: { "typeA:1.0": { x: 10, y: 10, width: 200, height: 150 } } }
                ];

                window.__mock_types = {
                    "0:253402214400000": [
                        { id: "typeA", version: "1.0", effect_date_time: 0, expire_date_time: infinity, attributes: [] },
                        { id: "typeB", version: "1.0", effect_date_time: futureStart, expire_date_time: infinity, attributes: [] }
                    ]
                };

                window.__mock_positions = {
                    "0:253402214400000": {
                        "typeA:1.0": { x: 10, y: 10, width: 200, height: 150 },
                        "typeB:1.0": { x: 250, y: 10, width: 200, height: 150 }
                    }
                };
            })()
        """.trimIndent())

        page.reload()
        // 워크스페이스 이벤트 발행하여 로딩 트리거
        page.evaluate("""
            (function() {
                var detail = { workspaceId: 'demo' };
                window.dispatchEvent(new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false}));
            })()
        """.trimIndent())
        
        page.waitForSelector(".type-box[data-type-key='typeA:1.0']")

        When("전 기간 유효한 타입 A에 타입 B 참조를 추가하려고 하면") {
            // 우클릭 메뉴를 통한 속성 추가
            page.click(".type-box[data-type-key='typeA:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT))
            page.click("text=Add Attribute")
            page.waitForSelector(".attr-editor-dialog.visible")
            
            // 속성 이름 입력
            page.evaluate("""
                (function() {
                    const field = document.querySelector('.attr-editor-dialog md-outlined-text-field:first-of-type');
                    if (field) {
                        field.value = 'ref-to-b';
                        field.dispatchEvent(new Event('input', { bubbles: true }));
                        field.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                })()
            """.trimIndent())

            // Document 타입 선택 및 참조 대상 입력

            page.click(".attr-type-btn:has-text('document')")
            page.evaluate("""
                (function() {
                    const select = document.querySelector('.validator-container md-outlined-select');
                    if (select) {
                        select.value = 'typeB';
                        select.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                })()
            """.trimIndent())
            page.click(".attr-edit-apply")

            Then("지능형 정합성 보정 다이얼로그가 표시된다") {
                page.waitForSelector("#conflict-resolution-dialog[open]")
            }

            When("소유자 기간 조정(Adjust Owner Period) 제안을 수락하면") {
                // 개편된 카드 UI의 Apply 버튼 클릭 (i18n 폴백 대응을 위해 'Adjust'로 완화)
                page.click(".conflict-proposal-card:has-text('Adjust') button")
                
                // 다이얼로그 닫힘 대기
                page.waitForSelector("#conflict-resolution-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))
                
                // 기간이 분할되면서 현재 뷰에서 타입이 사라졌을 수 있으므로, 미래 기간으로 이동 시도
                page.waitForTimeout(1000.0)
                if (page.locator(".type-box[data-type-key='typeA:1.0']").count() == 0) {
                    page.click(".type-ctrl-btn-after", com.microsoft.playwright.Page.ClickOptions().setForce(true))
                }
                
                Then("타입 A의 시작일이 타입 B가 존재하는 시점으로 자동 조정된다") {
                    page.waitForSelector(".type-box[data-type-key='typeA:1.0']")
                    page.click(".type-box[data-type-key='typeA:1.0'] .type-header")
                    
                    val datesLabel = page.locator(".type-status-header .type-property-dates")
                    val expectedDate = java.time.LocalDate.now().plusDays(10).toString()
                    
                    page.waitForFunction("el => el.textContent.includes('$expectedDate')", datesLabel.elementHandle())
                    datesLabel.textContent() shouldContain expectedDate
                }
                
                Then("속성 목록에 참조 속성이 성공적으로 추가된다") {
                    // .type-attr-row 내에 'ref-to-b'와 'typeB'가 모두 있는지 확인
                    val attrRow = page.locator(".type-box[data-type-key='typeA:1.0'] .type-attr-row:has-text('ref-to-b')")
                    attrRow.waitFor()
                    attrRow.textContent() shouldContain "typeB"
                }
            }
        }
    }

    Given("타입 간 참조가 설정된 환경") {
        page.setViewportSize(1280, 720)
        page.addInitScript("""
            (function() {
                const infinity = 253402214400000.0;
                window.__mock_layouts = [
                    { id: "l1", workspace: "demo", effect_date_time: 0, expire_date_time: infinity, positions: { "parent-type:1.0": { x: 10, y: 10, width: 200, height: 150 }, "child-type:1.0": { x: 250, y: 10, width: 200, height: 150 } } }
                ];
                window.__mock_types = {
                    "0:253402214400000": [
                        { id: "parent-type", version: "1.0", effect_date_time: 0, expire_date_time: infinity, attributes: [
                            { id: "attr-1", name: "childRef", type: { type: "document", referenced_type: "child-type" }, nullable: false, inherited: false, order: 1 }
                        ] },
                        { id: "child-type", version: "1.0", effect_date_time: 0, expire_date_time: infinity, attributes: [] }
                    ]
                };
                window.__mock_positions = {
                    "0:253402214400000": {
                        "parent-type:1.0": { x: 10, y: 10, width: 200, height: 150 },
                        "child-type:1.0": { x: 250, y: 10, width: 200, height: 150 }
                    }
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
        page.waitForSelector(".type-box[data-type-key='parent-type:1.0']")

        When("자식 타입의 이름을 변경하면") {
            // TYPE 모드로 전환
            page.locator(".type-mode-toggle button").nth(1).click()
            
            // 인라인 이름 편집 진입
            page.dblclick(".type-box[data-type-key='child-type:1.0'] .type-name")
            val input = page.locator(".type-box[data-type-key='child-type:1.0'] .type-name-input")
            input.fill("renamed-child")
            input.press("Enter")

            Then("참조 무결성 충돌 다이얼로그(Update References)가 표시된다") {
                val dialog = page.locator("#conflict-resolution-dialog[open]")
                dialog.waitFor()
                dialog.textContent() shouldContain "renamed or deleted"
            }

            When("Update References 제안을 수락하면") {
                page.click(".conflict-proposal-card:has-text('Update References') button")
                page.waitForSelector("#conflict-resolution-dialog", com.microsoft.playwright.Page.WaitForSelectorOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN))

                Then("부모 타입의 참조 대상이 새로운 이름으로 자동 업데이트된다") {
                    val attrRow = page.locator(".type-box[data-type-key='parent-type:1.0'] .type-attr-row:has-text('childRef')")
                    attrRow.waitFor()
                    attrRow.textContent() shouldContain "renamed-child"
                }
            }
        }
    }
})
