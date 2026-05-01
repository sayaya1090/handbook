package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("canvastest.html")
internal class TypeBulkActionTest: GwtTestSpec({
    Given("캔버스가 초기화됨") {
        Thread.sleep(5000)

        When("여러 타입을 선택한 뒤") {
            // customer:1.0 과 order:1.0 두 개가 기본으로 있음
            page.click(".type-box[data-type-key='customer:1.0']")
            page.keyboard().down("Control")
            page.click(".type-box[data-type-key='order:1.0']")
            page.keyboard().up("Control")
            Thread.sleep(500)
            
            And("Bulk Delete 버튼을 클릭하면") {
                page.click(".type-ctrl-btn-bulk-delete")
                Thread.sleep(500)
                
                And("Confirm 다이얼로그에서 Delete를 클릭하면") {
                    page.click("md-filled-button:has-text('Delete')")
                    Thread.sleep(1000)
                    
                    Then("선택된 타입들이 모두 삭제된다") {
                        val boxes = page.querySelectorAll(".type-box")
                        boxes.count() shouldBe 0
                    }
                }
            }
        }
        
        When("새 타입을 추가하고 Ctrl+A로 전체 선택한 뒤") {
            // 테스트 환경 리셋 (새로운 Given 블록이 아니므로 수동 리셋 필요할 수 있으나 GwtTestSpec은 각 Given 마다 새로 로딩함)
            page.reload()
            Thread.sleep(5000)
            
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            
            page.evaluate("document.querySelector('.type-canvas').focus()")
            page.keyboard().press("Control+A")
            Thread.sleep(500)
            
            And("Delete 키를 누르면") {
                page.keyboard().press("Delete")
                Thread.sleep(1000)
                
                Then("모든 타입이 삭제된다") {
                    val boxes = page.querySelectorAll(".type-box")
                    boxes.count() shouldBe 0
                }
            }
        }
    }
})
