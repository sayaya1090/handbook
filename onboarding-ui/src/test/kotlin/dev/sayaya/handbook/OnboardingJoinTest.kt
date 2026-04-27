package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("workspacetest.html")
internal class WorkspaceJoinTest: GwtTestSpec({
    Given("워크스페이스 JOIN UI가 초기화됨") {
        // UC-W2: JOIN 모드 전환
        Then("JOIN 섹션이 존재한다") {
            val sections = page.querySelectorAll(".ws-section")
            sections.count() shouldBe 2
        }
        Then("JOIN 섹션에 라디오 버튼이 존재한다") {
            val sections = page.querySelectorAll(".ws-section")
            val radio = sections[1].querySelector("md-radio")
            radio shouldNotBe null
        }
        Then("JOIN 섹션에 입력 필드가 존재한다") {
            val sections = page.querySelectorAll(".ws-section")
            val input = sections[1].querySelector("md-outlined-text-field.ws-section-input")
            input shouldNotBe null
        }

        When("JOIN 라디오를 선택하면") {
            val sections = page.querySelectorAll(".ws-section")
            sections[1].querySelector("md-radio")!!.click()
            Thread.sleep(200)
            Then("JOIN 라디오가 체크된 상태이다") {
                val checked = page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-radio')?.checked") as Boolean
                checked shouldBe true
            }
            Then("CREATE 라디오는 체크 해제된다") {
                val checked = page.evaluate("document.querySelectorAll('.ws-section')[0].querySelector('md-radio')?.checked") as Boolean
                checked shouldBe false
            }
            Then("Submit 버튼이 비활성 상태이다 (입력 없음)") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe true
            }
        }

        When("JOIN 라디오 선택 후 코드를 입력하면") {
            val sections = page.querySelectorAll(".ws-section")
            sections[1].querySelector("md-radio")!!.click()
            Thread.sleep(200)
            page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-outlined-text-field.ws-section-input').value = 'ABC-123'")
            page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-outlined-text-field.ws-section-input').dispatchEvent(new Event('input', {bubbles: true}))")
            Thread.sleep(300)
            Then("입력 필드에 입력한 값이 반영된다") {
                val value = page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-outlined-text-field.ws-section-input').value") as String
                value shouldBe "ABC-123"
            }
            Then("Submit 버튼이 활성화된다") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe false
            }
        }

        When("JOIN에서 CREATE로 모드를 전환하면") {
            val sections = page.querySelectorAll(".ws-section")
            sections[1].querySelector("md-radio")!!.click()
            Thread.sleep(200)
            page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-outlined-text-field.ws-section-input').value = 'SomeCode'")
            page.evaluate("document.querySelectorAll('.ws-section')[1].querySelector('md-outlined-text-field.ws-section-input').dispatchEvent(new Event('input', {bubbles: true}))")
            Thread.sleep(200)
            sections[0].querySelector("md-radio")!!.click()
            Thread.sleep(300)
            Then("Submit 버튼이 비활성 상태로 돌아간다 (CREATE 입력 없음)") {
                val btn = page.querySelector(".ws-submit")
                val disabled = btn!!.evaluate("el => el.disabled || el.hasAttribute('disabled')") as Boolean
                disabled shouldBe true
            }
        }
    }
})
