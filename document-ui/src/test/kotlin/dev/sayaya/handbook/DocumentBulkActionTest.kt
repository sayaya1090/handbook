package dev.sayaya.handbook

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitForSelectorState
import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("documenttest.html")
internal class DocumentBulkActionTest: GwtTestSpec({
     Given("문서 UI가 초기화됨") {
        When("여러 행을 추가한 뒤") {
            // Add rows and wait for them to appear
            page.click(".doc-ctrl-btn-add", Page.ClickOptions().setForce(true))
            page.waitForSelector(".ht_master tbody tr:nth-child(1) td", Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
            
            page.click(".doc-ctrl-btn-add", Page.ClickOptions().setForce(true))
            page.waitForSelector(".ht_master tbody tr:nth-child(2) td", Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
            
            And("행들을 체크박스로 선택하고") {
                // Target rows 3 and 4 (the newly added ones)
                page.click(".ht_clone_left tbody tr:nth-child(3) .htCheckboxRendererInput", Page.ClickOptions().setForce(true))
                page.click(".ht_clone_left tbody tr:nth-child(4) .htCheckboxRendererInput", Page.ClickOptions().setForce(true))
                Thread.sleep(500)
            }

            And("Bulk Status를 'REVIEW'로 변경하면") {
                page.waitForSelector(".doc-ctrl-bulk-status-select", Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
                page.selectOption(".doc-ctrl-bulk-status-select", "REVIEW", Page.SelectOptionOptions().setForce(true))
                
                // Wait for the status text change to propagate to the master table
                page.waitForFunction("""
                    () => {
                        var cell3 = document.querySelector('.ht_master tbody tr:nth-child(3) td:nth-child(3)');
                        var cell4 = document.querySelector('.ht_master tbody tr:nth-child(4) td:nth-child(3)');
                        return cell3 && cell3.textContent.trim() === 'REVIEW' && cell4 && cell4.textContent.trim() === 'REVIEW';
                    }
                """.trimIndent())
                
                Then("선택된 모든 행의 상태가 'REVIEW'로 변경된다") {
                    val status3 = page.evaluate("() => document.querySelector('.ht_master tbody tr:nth-child(3) td:nth-child(3)').textContent.trim()") as String
                    val status4 = page.evaluate("() => document.querySelector('.ht_master tbody tr:nth-child(4) td:nth-child(3)').textContent.trim()") as String
                    status3 shouldBe "REVIEW"
                    status4 shouldBe "REVIEW"
                }
            }

            And("모든 행을 체크박스로 선택하고") {
                // Select all 4 rows to test full bulk deletion
                val rowCount = page.evaluate("() => document.querySelectorAll('.ht_clone_left tbody tr').length") as Int
                for (i in 1..rowCount) {
                    val isChecked = page.evaluate("el => el.checked", page.querySelector(".ht_clone_left tbody tr:nth-child($i) .htCheckboxRendererInput")) as Boolean
                    if (!isChecked) {
                        page.click(".ht_clone_left tbody tr:nth-child($i) .htCheckboxRendererInput", Page.ClickOptions().setForce(true))
                    }
                }
                Thread.sleep(500)
            }

            And("Bulk Delete 버튼을 클릭하면") {
                page.click(".doc-ctrl-btn-bulk-delete", Page.ClickOptions().setForce(true))
                
                And("Confirm 다이얼로그에서 Delete를 클릭하면") {
                    page.waitForSelector(".ui-confirm-dialog", Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE))
                    // Correcting the button type and text selector based on actual HTML
                    page.click(".ui-confirm-actions md-text-button:has-text('Delete')", Page.ClickOptions().setForce(true))
                    
                    // Ensure the rows are actually removed
                    page.waitForFunction("() => document.querySelectorAll('.ht_master tbody tr td:not(.htNoWrap)').length === 0", null, Page.WaitForFunctionOptions().setTimeout(10000.0))
                    
                    Then("선택된 행들이 모두 삭제된다") {
                        val cellCount = page.evaluate("() => document.querySelectorAll('.ht_master tbody tr td:not(.htNoWrap)').length") as Int
                        cellCount shouldBe 0
                    }
                }
            }
        }
    }
})
