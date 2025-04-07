package dev.sayaya.handbook

import dev.sayaya.handbook.client.drawer.DrawerMock
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import java.io.File

internal class DrawerTest: BehaviorSpec({
    val document = ChromeDriver(ChromeOptions().addArguments("--headless"))
    val html = File("src/test/webapp/dev.sayaya.handbook.DrawerTest.html")
    document.get("file://${html.absolutePath}")

    Given("메뉴가 닫힌 상태") {
        val btnMenu = document.findElement(By.id("menu-toggle-button"))
        val rails = document.findElements(By.className("rail"))
        val nav = document.findElement(By.tagName("nav"))
        nav.getDomAttribute("open") shouldBe null
        When("메뉴 버튼을 누르면") {
            btnMenu.click()
            Then("nav 상태가 open으로 변경된다") {
                nav.getDomAttribute("open") shouldBe "true"
            }
            Then("Menu Rail이 확장되고 Page Menu는 숨겨진 상태를 유지한다") {
                rails[0].getDomAttribute("expand") shouldBe "true"
                rails[1].getDomAttribute("hide") shouldBe "true"
            }
            Then("Menu Rail에 모든 메뉴 항목이 표시된다") {
                rails[0].findElements(By.className("item")).size shouldBe DrawerMock.menu.size
            }
            Then("Bottom으로 표시된 메뉴 항목들은 하단에 모여 정렬된다") {
                val bottoms = DrawerMock.menu.filter { it.bottom == true }
                // Margin-top이 주어진 항목과 그 이하는 모두 하단 정렬로 간주
                rails[0].findElements(By.className("item")).count { i -> i.getCssValue("margin-top") != "0px" } shouldBe 1
                rails[0].findElements(By.className("item")).dropWhile { it.getCssValue("margin-top") == "0px"  }.count() shouldBe bottoms.size
            }
            And("메뉴 버튼을 다시 누르면") {
                btnMenu.click()
                Then("nav 요소의 open 속성이 제거된다") {
                    nav.getDomAttribute("open") shouldBe null
                }
            }
        }
    }
    Given("메뉴가 열림 상태") {
        val actions = Actions(document)
        val btnMenu = document.findElement(By.id("menu-toggle-button"))
        val rails = document.findElements(By.className("rail"))
        val nav = document.findElement(By.tagName("nav"))
        if(nav.getDomAttribute("open") == null) btnMenu.click()
        nav.getDomAttribute("open") shouldBe "true"

        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools.size shouldBeLessThanOrEqual 1
        }
        Then("두번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[1].tools.size shouldBeGreaterThan 1
        }
        When("첫번째 메뉴에 마우스 호버하면") {
            val menu1st = rails[0].findElements(By.className("item"))[0]
            actions.moveToElement(menu1st).perform()
            Then("Tool 레일은 숨김 상태를 유지한다") {

            }
            And("첫번째 메뉴를 클릭하면") {
                Then("Menu Rail이 Collapse 상태로 전환된다") {

                }
                Then("nav가 닫힘 상태로 변경된다") {
                    
                }
                Then("Tool 레일은 계속 숨김 상태를 유지한다") {

                }
                And("두번째 메뉴를 클릭하면") {
                    Then("Tool Rail이 Collapse 상태로 전환된다") {
                        
                    }
                    Then("Menu Rail이 Hide 상태로 전환된다") {

                    }
                }
            }
        }

        if(nav.getDomAttribute("open") == null) btnMenu.click()
        nav.getDomAttribute("open") shouldBe "true"
        When("두번째 메뉴에 마우스 호버하면") {
            val menu2nd = rails[0].findElements(By.className("item"))[1]
            actions.moveToElement(menu2nd).perform()
            Then("Tool Rail이 Expand 상태로 전환된다") {

            }
            Then("Tool Rail의 아이템은 두번째 메뉴와 높이가 정렬되어 있다") {

            }
            And("첫번째 Tool을 클릭하면") {
                Then("Tool Rail이 Collapse 상태로 전환된다") {

                }
                Then("Menu Rail이 Hide 상태로 전환된다") {

                }
                Then("nav가 닫힘 상태로 변경된다") {

                }
                And("두번째 Tool을 클릭하면") {
                    Then("Tool Rail이 Collapse 상태로 유지된다") {

                    }
                    Then("Menu Rail이 Hide 상태로 유지된다") {

                    }
                    Then("nav가 닫힘 상태로 유지된다") {

                    }
                }
                And("돌아가기 버튼을 클릭하면") {
                    Then("Tool Rail이 Hide 상태로 전환된다") {

                    }
                    Then("Menu Rail이 Collapse 상태로 전환된다") {

                    }
                    Then("nav가 닫힘 상태로 유지된다") {

                    }
                }
            }
        }
    }
    afterSpec {
        document.quit()
    }
})