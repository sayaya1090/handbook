package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/frame.html")
internal class FrameTest: GwtTestSpec({
    Given("렌더러가 준비됨") {
        Then("컨테이너(#container)가 DOM에 존재한다") {
            page.querySelector("#container") shouldNotBe null
        }

        Then("초기 상태에서 프레임이 없다") {
            val frames = page.querySelectorAll("#container .frame")
            frames.count() shouldBe 0
        }

        When("첫번째 렌더러를 활성화하면") {
            page.click("#renderer1")
            Thread.sleep(500)
            Then("프레임이 1개 추가된다") {
                val frames = page.querySelectorAll("#container .frame")
                frames.count() shouldBe 1
            }
            Then("프레임 텍스트가 'Hello, World!!'이다") {
                val frame = page.querySelector("#container .frame")
                frame shouldNotBe null
                frame!!.textContent() shouldBe "Hello, World!!"
            }
        }

        When("두번째 렌더러를 활성화하면") {
            page.click("#renderer2")
            Thread.sleep(1000)
            Then("새로운 프레임의 텍스트가 '2nd Renderer rendered'이다") {
                val frame = page.querySelector("#container .frame")
                frame shouldNotBe null
                frame!!.textContent() shouldBe "2nd Renderer rendered"
            }
            Then("프레임은 여전히 1개만 존재한다 (이전 프레임 교체)") {
                val frames = page.querySelectorAll("#container .frame")
                frames.count() shouldBe 1
            }
        }

        When("다시 첫번째 렌더러로 전환하면") {
            page.click("#renderer1")
            Thread.sleep(500)
            Then("프레임 텍스트가 다시 'Hello, World!!'로 전환된다") {
                val frame = page.querySelector("#container .frame")
                frame shouldNotBe null
                frame!!.textContent() shouldBe "Hello, World!!"
            }
            Then("프레임 개수는 1개로 유지된다") {
                val frames = page.querySelectorAll("#container .frame")
                frames.count() shouldBe 1
            }
        }
    }
})
