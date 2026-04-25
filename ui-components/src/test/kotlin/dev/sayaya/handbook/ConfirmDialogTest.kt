package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("uicomponentstest.html")
internal class ConfirmDialogTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("확인 다이얼로그 래퍼가 존재한다") {
            page.querySelector(".ui-confirm-wrapper") shouldNotBe null
        }

        When("Confirm 버튼을 클릭하면") {
            page.click("#btn-confirm")
            Thread.sleep(500)

            Then("다이얼로그가 표시된다") {
                val dialog = page.querySelector(".ui-confirm-dialog")
                dialog shouldNotBe null
            }

            Then("옵션 버튼 2개가 렌더링된다") {
                val buttons = page.querySelectorAll(".ui-confirm-option")
                buttons.count() shouldBe 2
            }

            Then("첫 번째 옵션 버튼 텍스트가 '삭제'이다") {
                val buttons = page.querySelectorAll(".ui-confirm-option")
                val firstBtn = buttons.first()
                firstBtn.textContent()!!.trim() shouldBe "삭제"
            }

            Then("두 번째 옵션 버튼 텍스트가 '취소'이다") {
                val buttons = page.querySelectorAll(".ui-confirm-option")
                val secondBtn = buttons.last()
                secondBtn.textContent()!!.trim() shouldBe "취소"
            }

            Then("옵션 버튼을 클릭하면 다이얼로그가 닫히고 콜백이 실행된다") {
                page.click(".ui-confirm-option")
                Thread.sleep(500)
                // 콜백은 toast.show(SUCCESS, "선택: 삭제")를 호출 -> SUCCESS 토스트 확인
                val toast = page.querySelector(".ui-toast-success")
                toast shouldNotBe null
                toast!!.textContent()!!.contains("선택: 삭제") shouldBe true
            }
        }

        // 다이얼로그 재표시
        When("다이얼로그를 닫은 후 다시 Confirm 버튼을 클릭하면") {
            page.click("#btn-confirm")
            Thread.sleep(500)

            Then("다이얼로그가 다시 표시된다") {
                val dialog = page.querySelector(".ui-confirm-dialog")
                dialog shouldNotBe null
            }

            Then("옵션 버튼이 다시 렌더링된다") {
                val buttons = page.querySelectorAll(".ui-confirm-option")
                buttons.count() shouldBe 2
            }
        }
    }
})
