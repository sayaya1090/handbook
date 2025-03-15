package dev.sayaya.handbook.client.box

import io.kotest.core.spec.style.BehaviorSpec
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

class TypeBoxTest : BehaviorSpec({
    Given("AA") {
        val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
        val html = File("src/test/webapp/dev.sayaya.handbook.TypeBox.html")
        val doc = document.get("file://${html.absolutePath}")

        When("BB") {
            Then("CECD") {
                val body = document.findElement(By.tagName("body"))
                println(body.text)
            }
        }

        // 생성, 모양, 크기, 색깔
        // 호버(모드)
        // 드래그 앤 드랍(모드)
        // 이동키(모드)
        // 타이핑(모드)
        // 컨텍스트메뉴
    }
})