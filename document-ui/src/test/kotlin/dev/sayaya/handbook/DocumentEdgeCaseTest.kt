package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentEdgeCaseTest: GwtTestSpec({
    Given("문서 UI 엣지 케이스") {
        // 마지막 페이지에서 Next 클릭
        When("마지막 페이지에서 Next 버튼을 클릭하면") {
            page.click(".doc-page-next")
            Thread.sleep(300)
            Then("스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
                val headers = page.querySelectorAll(".handsontable thead th")
                headers.count() shouldNotBe 0
            }
        }

        // 첫 페이지에서 Prev 클릭
        When("첫 페이지에서 Prev 버튼을 클릭하면") {
            page.click(".doc-page-prev")
            Thread.sleep(300)
            Then("스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // 행 선택 없이 Delete 클릭
        When("행을 선택하지 않고 Delete 버튼을 클릭하면") {
            val rowCount = page.querySelectorAll(".doc-spreadsheet tbody tr").count()
            page.click(".doc-ctrl-btn-delete")
            Thread.sleep(300)
            Then("행 수가 변하지 않는다") {
                val after = page.querySelectorAll(".doc-spreadsheet tbody tr").count()
                after shouldBe rowCount
            }
        }

        // malformed 이벤트
        When("malformed DOCUMENT 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var evt = new CustomEvent('handbook-workspace-event', {detail: 'DOCUMENT_CREATED:invalid_json', bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(500)
            Then("스프레드시트가 에러 없이 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
                val html = page.querySelector(".doc-spreadsheet")!!.innerHTML()
                html.isNotBlank() shouldBe true
            }
        }

        // 빠른 타입 전환
        When("타입 탭을 빠르게 전환하면") {
            val tabs = page.querySelectorAll(".doc-type-tab")
            if (tabs.count() >= 2) {
                page.click(".doc-type-tab:nth-child(1)")
                page.click(".doc-type-tab:nth-child(2)")
                page.click(".doc-type-tab:nth-child(1)")
                Thread.sleep(500)
                Then("스프레드시트가 유지되고 컬럼이 존재한다") {
                    val headers = page.querySelectorAll(".handsontable thead th")
                    headers.count() shouldNotBe 0
                }
            }
        }

        // 빈 mutate 이벤트
        When("빈 mutate 이벤트를 수신하면") {
            page.evaluate("""
                (function() {
                    var evt = new CustomEvent('handbook-mutate', {detail: [], bubbles: false});
                    window.dispatchEvent(evt);
                })()
            """.trimIndent())
            Thread.sleep(300)
            Then("스프레드시트가 유지된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }
    }
})
