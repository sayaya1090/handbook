package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/progress.html")
internal class ProgressTest: GwtTestSpec({
    Given("프로그레스 바가 준비됨") {
        Thread.sleep(3000)

        Then("프로그레스 컨테이너가 존재한다") {
            page.querySelector(".progress-container") shouldNotBe null
        }

        Then("초기 상태에서는 숨겨져 있다") {
            val container = page.querySelector(".progress-container")
            container shouldNotBe null
            val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
            opacity shouldBe "0"
        }

        When("Indeterminate 버튼을 클릭하면") {
            page.click("#btn-indeterminate")
            Thread.sleep(500)
            Then("프로그레스 바가 표시된다") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "1"
            }
            Then("라벨은 숨겨져 있다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val display = label!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
        }

        When("30% 버튼을 클릭하면") {
            page.click("#btn-30")
            Thread.sleep(500)
            Then("프로그레스 바가 표시된다") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "1"
            }
            Then("라벨에 설명이 표시된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                label!!.textContent()!!.contains("처리 중") shouldBe true
                label.textContent()!!.contains("3/10") shouldBe true
            }
        }

        When("100% 버튼을 클릭하면") {
            page.click("#btn-100")
            Thread.sleep(500)
            Then("라벨에 완료가 표시된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                label!!.textContent()!!.contains("완료") shouldBe true
                label.textContent()!!.contains("10/10") shouldBe true
            }
        }

        When("Hide 버튼을 클릭하면") {
            page.click("#btn-hide")
            Thread.sleep(500)
            Then("프로그레스 바가 숨겨진다") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "0"
            }
        }
    }
})
