package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 워크스페이스 전환 시 데이터 격리 및 초기화 검증 테스트.
 * UC-T25: 워크스페이스 전환 시 이전 워크스페이스의 데이터가 남지 않고 새 데이터로 완전히 교체되어야 한다.
 */
@GwtHtml("navigationtest.html")
internal class WorkspaceSwitchTest: GwtTestSpec({
    Given("서로 다른 워크스페이스 데이터 Mock 설정 및 페이지 로드") {
        page.setViewportSize(1280, 720)
        
        // 1. 초기화 스크립트 등록 (리로드 시 실행됨)
        page.addInitScript("""
            window.__mock_layouts_map = {
                "ws-1": [{ id: "l-ws1", workspace: "ws-1", effect_date_time: 0, expire_date_time: 253402214400000.0, positions: { "type-ws1:1.0": { x: 10, y: 10, width: 200, height: 150 } } }],
                "ws-2": [{ id: "l-ws2", workspace: "ws-2", effect_date_time: 0, expire_date_time: 253402214400000.0, positions: { "type-ws2:1.0": { x: 50, y: 50, width: 200, height: 150 } } }]
            };
            window.__mock_types_map = {
                "ws-1:0:253402214400000": [{ id: "type-ws1", version: "1.0", effect_date_time: 0, expire_date_time: 253402214400000.0, attributes: [] }],
                "ws-2:0:253402214400000": [{ id: "type-ws2", version: "1.0", effect_date_time: 0, expire_date_time: 253402214400000.0, attributes: [] }]
            };
            window.__mock_positions_map = {
                "ws-1:0:253402214400000": { "type-ws1:1.0": { x: 10, y: 10, width: 200, height: 150 } },
                "ws-2:0:253402214400000": { "type-ws2:1.0": { x: 50, y: 50, width: 200, height: 150 } }
            };
            window.__mock_layouts = [];
            window.__mock_types = {};
            window.__mock_positions = {};
        """.trimIndent())

        // 2. 페이지 리로드하여 스크립트 적용 및 GWT 앱 초기화
        page.reload()
        page.waitForSelector(".type-canvas")

        When("첫 번째 워크스페이스(ws-1)로 전환하면") {
            page.evaluate("""
                (function() {
                    window.__mock_layouts = window.__mock_layouts_map["ws-1"];
                    window.__mock_types = { "0:253402214400000": window.__mock_types_map["ws-1:0:253402214400000"] };
                    window.__mock_positions = { "0:253402214400000": window.__mock_positions_map["ws-1:0:253402214400000"] };
                    
                    var detail = { workspaceId: 'ws-1' };
                    window.dispatchEvent(new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false}));
                })()
            """.trimIndent())
            
            Then("ws-1의 타입(type-ws1)만 표시되어야 한다") {
                page.waitForSelector(".type-box[data-type-key='type-ws1:1.0']")
                page.querySelectorAll(".type-box").size shouldBe 1
            }
        }

        When("두 번째 워크스페이스(ws-2)로 전환하면") {
            page.evaluate("""
                (function() {
                    window.__mock_layouts = window.__mock_layouts_map["ws-2"];
                    window.__mock_types = { "0:253402214400000": window.__mock_types_map["ws-2:0:253402214400000"] };
                    window.__mock_positions = { "0:253402214400000": window.__mock_positions_map["ws-2:0:253402214400000"] };
                    
                    var detail = { workspaceId: 'ws-2' };
                    window.dispatchEvent(new CustomEvent('handbook-workspace-context', {detail: JSON.stringify(detail), bubbles: false}));
                })()
            """.trimIndent())
            
            Then("이전 워크스페이스의 타입(type-ws1)은 제거되어야 한다") {
                // 수정 전에는 이 부분에서 실패(size가 2)할 것으로 기대됨
                Thread.sleep(1000)
                page.locator(".type-box[data-type-key='type-ws1:1.0']").count() shouldBe 0
            }

            Then("새 워크스페이스의 타입(type-ws2)만 표시되어야 한다") {
                page.waitForSelector(".type-box[data-type-key='type-ws2:1.0']")
                page.querySelectorAll(".type-box").size shouldBe 1
            }
        }
    }
})
