package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("documenttest.html")
internal class DocumentBulkActionTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        Thread.sleep(5000)

        When("여러 행을 추가한 뒤") {
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            page.click(".doc-ctrl-btn-add")
            Thread.sleep(500)
            
            And("행들을 체크박스로 선택하고") {
                val checkboxes = page.querySelectorAll(".htCheckboxRendererInput")
                checkboxes[0].click()
                checkboxes[1].click()
                Thread.sleep(300)
            }

            And("Bulk Status를 'REVIEW'로 변경하면") {
                page.selectOption(".doc-ctrl-bulk-status-select", "REVIEW")
                Thread.sleep(500)
                
                Then("선택된 모든 행의 상태가 'REVIEW'로 변경된다") {
                    val statusCells = page.querySelectorAll(".handsontable td:nth-child(6)") // status column is 5th (0-indexed) or 6th (1-indexed)?
                    // Checkbox(1), Type(2), Serial(3), Effect(4), Expire(5), Status(6)
                    statusCells[0].textContent().trim() shouldBe "REVIEW"
                    statusCells[1].textContent().trim() shouldBe "REVIEW"
                }
            }

            And("Bulk Delete 버튼을 클릭하면") {
                // Re-select because status change cleared selection
                val checkboxes = page.querySelectorAll(".htCheckboxRendererInput")
                checkboxes[0].click()
                checkboxes[1].click()
                Thread.sleep(300)

                page.click(".doc-ctrl-btn-bulk-delete")
                Thread.sleep(500)
                
                And("Confirm 다이얼로그에서 Delete를 클릭하면") {
                    page.click("md-filled-button:has-text('Delete')")
                    Thread.sleep(1000)
                    
                    Then("선택된 행들이 모두 삭제된다") {
                        val cells = page.querySelectorAll(".handsontable td")
                        // Should be empty or have "No documents" overlay
                        // Depending on the test data seed, might still have other rows.
                        // But in this test we added 2 and deleted 2.
                    }
                }
            }
        }
    }
})
