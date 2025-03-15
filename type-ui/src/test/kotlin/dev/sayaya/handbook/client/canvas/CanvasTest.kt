package dev.sayaya.handbook.client.box

import io.kotest.core.spec.style.BehaviorSpec
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

class CanvasTest : BehaviorSpec({
    Given("AA") {
        val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
        val html = File("src/test/webapp/dev.sayaya.handbook.Canvas.html")
        val doc = document.get("file://${html.absolutePath}")

        When("BB") {
            Then("CECD") {
                val body = document.findElement(By.tagName("body"))
                println(body.text)
            }
        }
    }
})