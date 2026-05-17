package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

/**
 * 플로팅 툴바 가시성 및 버튼 활성화 로직 검증 테스트.
 * 타입을 선택하지 않아도 툴바가 보여야 하며, 선택 상태에 따라 버튼이 활성화되어야 한다.
 */
@GwtHtml("canvastest.html")
internal class FloatingToolbarTest: GwtTestSpec({
    Given("타입 편집기 초기화됨") {
        page.setViewportSize(1280, 720)
        page.reload()
        page.waitForSelector(".type-box[data-type-key='customer:1.0']")

        Then("타입을 선택하지 않아도 플로팅 툴바가 화면에 보여야 한다") {
            page.waitForSelector(".type-floating-toolbar.visible")
        }

        Then("상단 바에 기간 정보가 상시 노출되어야 한다") {
            page.waitForSelector(".type-nav-group")
            page.locator(".type-period-label").isVisible shouldBe true
        }

        Then("초기 상태에서 상단 속성 바는 비어있는 상태('-')로 노출된다") {
            page.locator(".type-status-header .type-property-id").textContent() shouldBe "-"
            page.locator(".type-status-header .type-property-version").textContent() shouldBe "-"
        }

        When("타입(customer:1.0)을 하나 선택하면") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            
            Then("상단 속성 바에 타입 정보가 표시되고, 기간 정보도 여전히 유지된다") {
                page.locator(".type-status-header .type-property-id").textContent() shouldBe "customer"
                page.locator(".type-status-header .type-property-version").textContent() shouldBe "1.0"
                page.locator(".type-period-label").isVisible shouldBe true
            }

            Then("삭제 버튼과 새 버전 버튼이 모두 활성화된다") {
                page.waitForFunction("() => !document.querySelector('.type-ctrl-btn-remove').disabled")
                page.waitForFunction("() => !document.querySelector('.type-ctrl-btn-new-version').disabled")
            }
        }

        When("캔버스 빈 영역을 클릭하여 선택을 해제하면") {
            page.click(".type-canvas", com.microsoft.playwright.Page.ClickOptions().setPosition(10.0, 10.0))
            
            Then("툴바는 여전히 보이지만 버튼들은 다시 비활성화된다") {
                page.waitForSelector(".type-floating-toolbar.visible")
                page.locator(".type-ctrl-btn-remove").getAttribute("disabled") shouldBe ""
                page.locator(".type-ctrl-btn-new-version").getAttribute("disabled") shouldBe ""
            }
        }

        When("여러 타입을 선택하면 (customer:1.0 + order:1.0)") {
            page.click(".type-box[data-type-key='customer:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            page.keyboard().down("Control")
            page.click(".type-box[data-type-key='order:1.0'] .type-header", com.microsoft.playwright.Page.ClickOptions().setForce(true))
            page.keyboard().up("Control")

            Then("삭제 버튼은 활성화되지만, 새 버전 버튼은 비활성화된다 (단일 선택 전용)") {
                page.waitForFunction("() => !document.querySelector('.type-ctrl-btn-remove').disabled")
                page.locator(".type-ctrl-btn-new-version").getAttribute("disabled") shouldBe ""
            }
        }
    }
})
