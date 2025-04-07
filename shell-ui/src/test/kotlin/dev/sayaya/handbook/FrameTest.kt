package dev.sayaya.handbook

import io.kotest.core.spec.style.BehaviorSpec
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

internal class FrameTest: BehaviorSpec({
    val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
    val html = File("src/test/webapp/dev.sayaya.handbook.FrameTest.html")
    document.get("file://${html.absolutePath}")
})