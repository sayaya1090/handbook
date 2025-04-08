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
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import java.io.File
import java.util.logging.Level


internal class DrawerTest: BehaviorSpec({
    val document = ChromeDriver(ChromeOptions().addArguments("--headless").apply {
        val logPrefs = LoggingPreferences()
        logPrefs.enable(LogType.BROWSER, Level.ALL)
        setCapability("goog:loggingPrefs", logPrefs)
    })
    val html = File("src/test/webapp/dev.sayaya.handbook.DrawerTest.html")
    document.get("file://${html.absolutePath}")
    val btnMenu = document.findElement(By.id("menu-toggle-button"))
    val rails = document.findElements(By.className("rail"))
    val drawer = document.findElement(By.tagName("nav"))
    val menuRail = rails[0]
    val toolRail = rails[1]
    Given("메뉴가 닫힌 상태") {
        drawer.getDomAttribute("open") shouldBe null
        When("메뉴 버튼을 누르면") {
            btnMenu.click()
            Then("드로어 상태가 open으로 변경된다") {
                drawer.getDomAttribute("open") shouldBe "true"
            }
            Then("Menu 레일이 확장 상태로 변경된다") {
                menuRail.getDomAttribute("expand") shouldBe "true"
            }
            Then("Tool 레일은 숨김 상태를 유지한다") {
                toolRail.getDomAttribute("hide") shouldBe "true"
            }
            Then("Menu 레일에 모든 메뉴 항목이 표시된다") {
                menuRail.findElements(By.className("item")).size shouldBe DrawerMock.menu.size
            }
            Then("Bottom으로 표시된 메뉴 항목들은 하단에 모여 정렬된다") {
                val bottoms = DrawerMock.menu.filter { it.bottom == true }
                // Margin-top이 주어진 항목과 그 이하는 모두 하단 정렬로 간주
                menuRail.findElements(By.className("item")).count { i -> i.getCssValue("margin-top") != "0px" } shouldBe 1
                menuRail.findElements(By.className("item")).dropWhile { it.getCssValue("margin-top") == "0px"  }.count() shouldBe bottoms.size
            }
            And("메뉴 버튼을 다시 누르면") {
                btnMenu.click()
                Then("드로어 요소의 open 속성이 제거된다") {
                    drawer.getDomAttribute("open") shouldBe null
                }
            }
        }
    }
    Given("메뉴가 열림 상태") {
        val actions = Actions(document)
        if(drawer.getDomAttribute("open") == null) btnMenu.click()
        drawer.getDomAttribute("open") shouldBe "true"

        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools.size shouldBeLessThanOrEqual 1
        }
        Then("두번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[1].tools.size shouldBeGreaterThan 1
        }
        When("첫번째 메뉴에 마우스 호버하면") {
            val menu1st = menuRail.findElements(By.className("item"))[0]
            actions.moveToElement(menu1st).perform()
            Then("Tool 레일은 숨김 상태를 유지한다") {
                toolRail.getDomAttribute("hide") shouldBe "true"
            }
            And("첫번째 메뉴를 클릭하면") {
                menu1st.click()
                Then("첫번째 메뉴의 첫번째 툴에 정의된 함수가 실행된다") {
                    document shouldContainLog "Menu1 Tool1 Clicked"
                }
                Then("Menu 레일이 Collapse 상태로 전환된다") {
                    menuRail.getDomAttribute("collapse") shouldBe "true"
                }
                Then("드로어가 닫힘 상태로 변경된다") {
                    drawer.getDomAttribute("open") shouldBe null
                }
                Then("Tool 레일은 계속 숨김 상태를 유지한다") {
                    toolRail.getDomAttribute("hide") shouldBe "true"
                }
                Then("첫번째 메뉴가 선택 상태로 변경된다") {
                    menu1st.getDomAttribute("selected") shouldBe "true"
                }
                val menu2nd = menuRail.findElements(By.className("item"))[1]
                And("두번째 메뉴를 호버하면") {
                    actions.moveToElement(menu2nd).perform()
                    Then("Tool 레일은 계속 숨김 상태를 유지한다") {
                        toolRail.getDomAttribute("hide") shouldBe "true"
                    }
                }
                And("두번째 메뉴를 클릭하면") {
                    menu2nd.click()
                    Then("Tool 레일이 Collapse 상태로 전환된다") {
                        toolRail.getDomAttribute("collapse") shouldBe "true"
                    }
                    Then("Menu 레일이 Hide 상태로 전환된다") {
                        menuRail.getDomAttribute("hide") shouldBe "true"
                    }
                    When("첫번째 툴을 클릭하면") {
                        Thread.sleep(100)
                        val tool1st = toolRail.findElements(By.className("item"))[0]
                        tool1st.click()
                        Then("툴에 정의된 함수가 실행된다") {
                            document shouldContainLog("Menu2 Tool1 Clicked")
                        }
                        Then("두번째 메뉴가 선택 상태로 변경된다") {
                            menu2nd.getDomAttribute("selected") shouldBe "true"
                        }
                    }
                }
            }
        }

        if(drawer.getDomAttribute("open") == null) btnMenu.click()
        drawer.getDomAttribute("open") shouldBe "true"
        When("메뉴를 열어서 두번째 메뉴에 마우스 호버하면") {
            val menu2nd = menuRail.findElements(By.className("item"))[1]
            actions.moveToElement(menu2nd).perform()
            Then("Tool 레일이 Expand 상태로 전환된다") {
                toolRail.getDomAttribute("expand") shouldBe "true"
            }
            Then("Tool 레일의 아이템은 두번째 메뉴와 높이가 정렬되어 있다") {
                val tool1st = toolRail.findElements(By.className("item"))[0]
                val offsetTopTool1st = tool1st.location.y
                val offsetTopMenu2nd = menu2nd.location.y
                offsetTopTool1st shouldBe offsetTopMenu2nd
            }
            And("두번째 Tool을 클릭하면") {
                val tool2nd = toolRail.findElements(By.className("item"))[1]
                tool2nd.click()
                Then("Tool 레일이 Collapse 상태로 전환된다") {
                    toolRail.getDomAttribute("collapse") shouldBe "true"
                }
                Then("Menu 레일이 Hide 상태로 전환된다") {
                    menuRail.getDomAttribute("hide") shouldBe "true"
                }
                Then("드로어가 닫힘 상태로 변경된다") {
                    drawer.getDomAttribute("open") shouldBe null
                }
                Then("툴에 정의된 함수가 실행된다") {
                    document shouldContainLog "Menu2 Tool2 Clicked"
                }
                And("첫번째 Tool을 클릭하면") {
                    val tool1st = toolRail.findElements(By.className("item"))[0]
                    tool1st.click()
                    Then("Tool 레일이 Collapse 상태로 유지된다") {
                        toolRail.getDomAttribute("collapse") shouldBe "true"
                    }
                    Then("Menu 레일이 Hide 상태로 유지된다") {
                        menuRail.getDomAttribute("hide") shouldBe "true"
                    }
                    Then("드로어가 닫힘 상태로 유지된다") {
                        drawer.getDomAttribute("open") shouldBe null
                    }
                    Then("툴에 정의된 함수가 실행된다") {
                        document shouldContainLog "Menu2 Tool1 Clicked"
                    }
                }
                And("돌아가기 버튼을 클릭하면") {
                    val back = toolRail.findElement(By.id("close-tool-rail"))
                    back.click()
                    Then("Tool 레일이 Hide 상태로 전환된다") {
                        toolRail.getDomAttribute("hide") shouldBe "true"
                    }
                    Then("Menu 레일이 Collapse 상태로 전환된다") {
                        menuRail.getDomAttribute("collapse") shouldBe "true"
                    }
                    Then("드로어가 닫힘 상태로 유지된다") {
                        drawer.getDomAttribute("open") shouldBe null
                    }
                }
            }
        }
    }
    afterSpec {
        document.quit()
    }
}) {
    companion object {
        infix fun ChromeDriver.shouldContainLog(expectedText: String) {
            val logEntries = this.manage().logs().get(LogType.BROWSER)
            val foundLog = logEntries.any { log ->
                log.message.contains(expectedText)
            }
            foundLog shouldBe true
            executeScript("console.clear();")
        }
    }
}