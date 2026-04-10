package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

@GwtHtml("src/test/webapp/progress.html")
internal class ProgressTest: GwtTestSpec({
    Given("프로그레스 바가 준비됨") {
        Then("프로그레스 컨테이너(.progress-container)가 DOM에 존재한다") {
            page.querySelector(".progress-container") shouldNotBe null
        }

        Then("초기 상태에서 opacity가 0이다 (숨김)") {
            val container = page.querySelector(".progress-container")
            container shouldNotBe null
            val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
            opacity shouldBe "0"
        }

        Then("프로그레스 라벨(.progress-label) 요소가 존재한다") {
            page.querySelector(".progress-label") shouldNotBe null
        }

        When("Indeterminate 버튼을 클릭하면") {
            page.click("#btn-indeterminate")
            Thread.sleep(500)
            Then("프로그레스 바 opacity가 1이 된다 (표시)") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "1"
            }
            Then("라벨 display가 none이다 (indeterminate 모드에서 라벨 숨김)") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                val display = label!!.evaluate("el => getComputedStyle(el).display") as String
                display shouldBe "none"
            }
        }

        When("30% 버튼을 클릭하면") {
            page.click("#btn-30")
            Thread.sleep(500)
            Then("프로그레스 바 opacity가 1이다") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "1"
            }
            Then("라벨에 '처리 중' 텍스트가 포함된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                label!!.textContent()!!.shouldContain("처리 중")
            }
            Then("프로그레스 바의 value가 3이다") {
                val bar = page.querySelector("md-linear-progress")
                bar shouldNotBe null
                val value = bar!!.evaluate("el => el.value") as Number
                value.toInt() shouldBe 3
            }
        }

        When("70% 버튼을 클릭하면") {
            page.click("#btn-70")
            Thread.sleep(500)
            Then("라벨에 '거의 완료' 텍스트가 포함된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                label!!.textContent()!!.shouldContain("거의 완료")
            }
            Then("프로그레스 바의 value가 7이다") {
                val bar = page.querySelector("md-linear-progress")
                bar shouldNotBe null
                val value = bar!!.evaluate("el => el.value") as Number
                value.toInt() shouldBe 7
            }
        }

        When("100% 버튼을 클릭하면") {
            page.click("#btn-100")
            Thread.sleep(500)
            Then("라벨에 '완료' 텍스트가 포함된다") {
                val label = page.querySelector(".progress-label")
                label shouldNotBe null
                label!!.textContent()!!.shouldContain("완료")
            }
            Then("프로그레스 바의 value가 10이다") {
                val bar = page.querySelector("md-linear-progress")
                bar shouldNotBe null
                val value = bar!!.evaluate("el => el.value") as Number
                value.toInt() shouldBe 10
            }
        }

        When("Hide 버튼을 클릭하면") {
            page.click("#btn-hide")
            Thread.sleep(500)
            Then("프로그레스 바 opacity가 0이 된다 (숨김)") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "0"
            }
        }

        When("Hide 후 다시 Indeterminate를 클릭하면") {
            page.click("#btn-hide")
            Thread.sleep(300)
            val beforeOpacity = page.querySelector(".progress-container")!!.evaluate("el => getComputedStyle(el).opacity") as String
            beforeOpacity shouldBe "0"
            page.click("#btn-indeterminate")
            Thread.sleep(500)
            Then("프로그레스 바가 다시 표시된다 (opacity 1)") {
                val container = page.querySelector(".progress-container")
                val opacity = container!!.evaluate("el => getComputedStyle(el).opacity") as String
                opacity shouldBe "1"
            }
        }
    }
})
