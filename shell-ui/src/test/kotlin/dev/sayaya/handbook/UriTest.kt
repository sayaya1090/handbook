package dev.sayaya.handbook

import dev.sayaya.handbook.client.drawer.DrawerMock
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File

internal class UriTest: BehaviorSpec({
    val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
    val html = File("src/test/webapp/dev.sayaya.handbook.DrawerTest.html")
    document.get("file://${html.absolutePath}")
    val btnMenu = document.findElement(By.id("menu-toggle-button"))
    val rails = document.findElements(By.className("rail"))
    val drawer = document.findElement(By.tagName("nav"))
    val menuRail = rails[0]
    val toolRail = rails[1]
    Given("메뉴 초기화") {
        btnMenu.click() // 메뉴버튼을 눌러 초기화
        val frame = document.findElement(By.className("frame"))
        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools().size shouldBeLessThanOrEqual 1
        }
        Then("세번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[2].tools().size shouldBeGreaterThan 1
        }
        When("첫번째 메뉴에 배정된 URL로 변경 요청을 하면") {
            frame.findElement(By.id("url1")).click()
            val url1st = document.currentUrl
            Then("URL이 변경된다") {
                url1st shouldNotBe null
                url1st!!.endsWith("/menu1-tool1")
            }
            Then("Tool 레일이 Hide 상태로 전환된다") {
                toolRail.getDomAttribute("hide") shouldBe "true"
            }
            Then("Menu 레일이 Collapse 상태로 전환된다") {
                menuRail.getDomAttribute("collapse") shouldBe "true"
            }
            Then("드로어가 닫힘 상태로 전환된다") {
                drawer.getDomAttribute("open") shouldBe null
            }
            Then("첫번째 메뉴가 활성화된다") {
                val menu1st = menuRail.findElements(By.className("item"))[0]
                menu1st.getDomAttribute("selected") shouldBe "true"
                menu1st.getDomProperty("innerHTML") shouldContain "Menu 1"
            }
            And("세번째 메뉴 첫번째 Tool에 배정된 URL로 변경 요청을 하면") {
                frame.findElement(By.id("url2")).click()
                val url2nd = document.currentUrl
                Then("URL이 변경된다") {
                    url2nd shouldNotBe null
                    url2nd!!.endsWith("/menu3-tool1")
                }

                Then("Tool 레일이 Collapse 상태로 전환된다") {
                    toolRail.getDomAttribute("collapse") shouldBe "true"
                }
                Then("Menu 레일이 Hide 상태로 전환된다") {
                    menuRail.getDomAttribute("hide") shouldBe "true"
                }
                Then("드로어가 닫힘 상태로 유지된다") {
                    drawer.getDomAttribute("open") shouldBe null
                }
                Then("세번째 메뉴의 첫번째 Tool이 활성화된다") {
                    val tool1st = toolRail.findElements(By.className("item"))[0]
                    tool1st.getDomAttribute("selected") shouldBe "true"
                    tool1st.getDomProperty("innerHTML") shouldContain "menu3-tool1"
                }
                And("세번째 메뉴 두번째 Tool에 배정된 URL로 변경 요청을 하면") {
                    frame.findElement(By.id("url3")).click()
                    val url3rd = document.currentUrl
                    Then("URL이 변경된다") {
                        url3rd shouldNotBe null
                        url3rd!!.endsWith("/menu3-tool2")
                    }
                    Then("Tool 레일이 Collapse 상태로 유지된다") {
                        toolRail.getDomAttribute("collapse") shouldBe "true"
                    }
                    Then("Menu 레일이 Hide 상태로 유지된다") {
                        menuRail.getDomAttribute("hide") shouldBe "true"
                    }
                    Then("드로어가 닫힘 상태로 유지된다") {
                        drawer.getDomAttribute("open") shouldBe null
                    }
                    Then("세번째 메뉴의 첫번째 Tool이 비활성화된다") {
                        val tool1st = toolRail.findElements(By.className("item"))[0]
                        tool1st.getDomAttribute("selected") shouldBe null
                        tool1st.getDomProperty("innerHTML") shouldContain "menu3-tool1"
                    }
                    Then("세번째 메뉴의 두번째 Tool이 활성화된다") {
                        val tool2nd = toolRail.findElements(By.className("item"))[1]
                        tool2nd.getDomAttribute("selected") shouldBe "true"
                        tool2nd.getDomProperty("innerHTML") shouldContain "menu3-tool2"
                    }
                }
            }
        }
    }
    afterSpec {
        document.quit()
    }
})