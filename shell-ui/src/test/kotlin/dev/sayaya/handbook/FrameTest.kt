package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/frame.html")
internal class FrameTest: GwtTestSpec({
    Given("렌더러가 준비됨") {
        Thread.sleep(3000)

        Then("컨테이너가 존재한다") {
            page.querySelector("#container") shouldNotBe null
        }

        When("첫번째 렌더러를 활성화하면") {
            page.click("#renderer1")
            Thread.sleep(500)
            Then("프레임이 추가된다") {
                val frame = page.querySelector("#container .frame")
                frame shouldNotBe null
                frame!!.textContent() shouldBe "Hello, World!!"
            }
        }

        When("두번째 렌더러를 활성화하면") {
            page.click("#renderer2")
            Thread.sleep(1000)
            Then("새로운 프레임이 추가된다") {
                val frame = page.querySelector("#container .frame")
                frame shouldNotBe null
                frame!!.textContent() shouldBe "2nd Renderer rendered"
            }
            Then("프레임은 1개만 존재한다") {
                val frames = page.querySelectorAll("#container .frame")
                frames.count() shouldBe 1
            }
        }
    }
})
