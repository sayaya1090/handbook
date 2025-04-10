package dev.sayaya.handbook

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

internal class FrameTest: BehaviorSpec({
    val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
    val html = File("src/test/webapp/dev.sayaya.handbook.FrameTest.html")
    document.get("file://${html.absolutePath}")
    val container = document.findElement(By.id("container"))
    Given("렌더러가 준비됨") {
        val renderer1 = document.findElement(By.id("renderer1"))
        val renderer2 = document.findElement(By.id("renderer2"))
        When("첫번째 렌더러를 활성화하면") {
            renderer1.click()
            val frame1 = container.findElement(By.className("frame"))
            Then("프레임이 추가된다") {
                frame1.isDisplayed shouldBe true
                frame1.text shouldBe "Hello, World!!"
            }
            And("두번째 렌더러를 활성화하면") {
                renderer2.click()
                Thread.sleep(1000)
                Then("새로운 프레임이 추가된다") {
                    val frame2 = container.findElement(By.className("frame"))
                    frame2.isDisplayed shouldBe true
                    frame2.text shouldBe "2nd Renderer rendered"
                }
                Then("기존 프레임은 삭제된다") {
                    val frames = container.findElements(By.className("frame"))
                    frames.size shouldBe 1
                    frame1 should beStale()
                }
            }
        }
    }
}) {
    companion object {
        fun beStale(): Matcher<WebElement> = object : Matcher<WebElement> {
            override fun test(value: WebElement): MatcherResult {
                return try {
                    value.isDisplayed
                    MatcherResult(
                        false,
                        { "Element should be stale but was still accessible" },
                        { "Element was unexpectedly stale" }
                    )
                } catch (e: StaleElementReferenceException) {
                    MatcherResult(
                        true,
                        { "Element should be stale" },
                        { "Element should not be stale" }
                    )
                }
            }
        }
    }
}