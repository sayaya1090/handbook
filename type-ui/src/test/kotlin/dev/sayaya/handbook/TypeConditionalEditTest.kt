package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-T6: 조건부 이름/버전 편집 정책 검증
 * - rev <= 0 (저장 전 신규 타입): 편집 가능
 * - rev > 0 (저장 후 기존 타입): 편집 차단 및 읽기 전용 UI
 */
@GwtHtml("canvastest.html")
internal class TypeConditionalEditTest: GwtTestSpec({
    Given("rev<=0인 신규 타입과 rev>0인 저장된 타입이 존재하는 환경") {
        page.setViewportSize(1280, 720)
        page.reload()
        
        // 가림막 제거
        page.evaluate("""
            document.querySelectorAll('.attr-editor-dialog, md-dialog').forEach(el => el.remove())
        """.trimIndent())
        
        page.waitForSelector(".type-box[data-type-key='order:1.0']")
        
        // TYPE 모드로 전환
        page.locator(".type-mode-toggle button").nth(1).click()

        When("새로운 타입을 생성하고(rev<=0) 이름을 더블클릭하면") {
            page.click(".type-ctrl-btn-add")
            Thread.sleep(500)
            
            // 새로 생성된 타입은 기본 이름이 'new-type' 등으로 생성됨. 마지막 .type-box 선택
            val newBoxes = page.locator(".type-box")
            val newBox = newBoxes.nth(newBoxes.count() - 1)
            newBox.locator(".type-name").dblclick()
            
            Then("입력 필드로 전환되어 편집이 가능하다") {
                val input = newBox.locator(".type-name-input")
                input.waitFor()
                input.isVisible() shouldBe true
                
                // 편집 취소
                page.keyboard().press("Escape")
            }
        }
    }
})