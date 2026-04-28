package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("drawer.html")
internal class ToolIntegrationTest: GwtTestSpec({
    Given("ToolProvider 브릿지가 초기화됨") {
        Then("ToolPublisher가 활성화되어 있다") {
            // ToolPublisher.register()가 호출되면 window.__handbook_tool_publisher 가 함수로 등록됨
            val registered = page.evaluate("typeof window.__handbook_tool_publisher === 'function'").toString()
            registered shouldBe "true"
        }
        When("외부 모듈이 도구 목록을 발행하면") {
            page.evaluate("""
                window.__handbook_tool_publisher([
                    { id: 'tool-1', icon: 'fa-plus', title: 'Add Tool', order: '001' },
                    { id: 'tool-2', icon: 'fa-trash', title: 'Delete Tool', order: '002' }
                ])
            """.trimIndent())
            Thread.sleep(500)
            Then("Tool Rail에 발행된 도구들이 렌더링된다") {
                // close 버튼을 제외하고 발행된 도구만 카운트 (data-tool-title 속성 존재 여부)
                val tools = page.querySelectorAll(".tool-rail .item[data-tool-title]")
                tools.count() shouldBe 2
            }
            Then("도구의 아이콘과 타이틀이 올바르게 설정된다") {
                val firstToolTitle = page.evaluate(
                    "document.querySelector('.tool-rail .item:nth-child(1)').dataset.toolTitle"
                ).toString()
                firstToolTitle shouldBe "Add Tool"
            }
        }
        When("렌더링된 도구를 클릭하면") {
            // ToolSubscriber.register()가 호출되면 window.__handbook_tool_subscriber 가 함수로 등록됨
            // 테스트에서는 이를 가로채서 클릭 이벤트가 전달되는지 확인
            page.evaluate("""
                window.__handbook_tool_selected_id = null;
                const originalSubscriber = window.__handbook_tool_subscriber;
                window.__handbook_tool_subscriber = function(id) {
                    window.__handbook_tool_selected_id = id;
                    if (originalSubscriber) originalSubscriber(id);
                }
            """.trimIndent())
            page.click(".tool-rail .item[data-tool-title='Delete Tool']")
            Thread.sleep(200)
            Then("ToolSubscriber를 통해 선택된 도구 ID가 전파된다") {
                val selectedId = page.evaluate("window.__handbook_tool_selected_id").toString()
                selectedId shouldBe "tool-2"
            }
        }
    }
})
