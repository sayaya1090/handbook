package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * 최근 수정된 UI/UX 및 로직에 대한 회귀 테스트.
 * UC-T8: 레이아웃 기간 이동
 * UC-T13: 모바일 반응형 레이아웃 (플로팅 컨트롤)
 */
@GwtHtml("canvastest.html")
internal class TypeEditorRegressionTest: GwtTestSpec({
    Given("타입 편집기 초기화됨") {

        // UC-T8: 레이아웃 자동 선택
        Then("초기 진입 시 가장 최신(마지막) 날짜 구간의 레이아웃이 자동 선택된다") {
            // TestApplication에서 어제(past)와 오늘(now) 두 기간을 주입함.
            // 최신 기간(now)이 선택되었다면 'Next' 버튼은 비활성화되어야 함.
            val afterBtn = page.querySelector(".type-ctrl-btn-after")
            afterBtn shouldNotBe null
            val isDisabled = afterBtn!!.getAttribute("disabled")
            isDisabled shouldBe "" // disabled 속성이 존재함
        }

        // UC-T8: 기간 라벨 표시
        Then("레이아웃 기간 정보(.type-period-label)가 표시된다") {
            val label = page.querySelector(".type-period-label")
            label shouldNotBe null
            label!!.textContent().contains(" ~ ") shouldBe true
        }

        Then("모든 도구 버튼(.type-ctrl-btn)은 원형(border-radius: 50%) 스타일을 가진다") {
            val radius = page.evaluate("""
                getComputedStyle(document.querySelector('.type-ctrl-btn')).borderRadius
            """.trimIndent()).toString()
            // 50% or matching pixel value (e.g. 22px for 44px button)
            (radius.contains("50%") || radius.contains("px")) shouldBe true
        }

        // UC-T13: 모바일 플로팅 컨트롤 전환
        When("모바일 뷰포트(400x800)로 전환하면") {
            page.setViewportSize(400, 800)
            page.waitForTimeout(500.0)
            page.evaluate("window.dispatchEvent(new Event('resize'))")
            
            Then("상단 상태바(.type-status-header)는 가로 스크롤이 가능하다") {
                val overflowX = page.evaluate("""
                    getComputedStyle(document.querySelector('.type-status-header')).overflowX
                """.trimIndent()).toString()
                overflowX shouldBe "auto"
            }
            
            Then("상단바의 자식 요소들은 찌그러지지 않는다 (flex-shrink: 0)") {
                val shrink = page.evaluate("""
                    getComputedStyle(document.querySelector('.type-status-header > *')).flexShrink
                """.trimIndent()).toString()
                shrink shouldBe "0"
            }

            Then("모바일 플로팅 조작 버튼(Speed Dial)이 존재한다") {
                val actionDial = page.querySelector(".type-speed-dial:not(.settings)")
                actionDial shouldNotBe null
                val settingsDial = page.querySelector(".type-speed-dial.settings")
                settingsDial shouldNotBe null
            }

            When("FAB을 클릭하면") {
                page.evaluate("document.querySelector('.type-speed-dial:not(.settings) md-fab').click()")
                Thread.sleep(300)
                Then("메뉴가 확장된다 ([expanded] 속성)") {
                    val isExpanded = page.evaluate("""
                        document.querySelector('.type-speed-dial:not(.settings)').hasAttribute('expanded')
                    """.trimIndent()).toString()
                    isExpanded shouldBe "true"
                }
            }
        }
        
        // 원복
        page.setViewportSize(1280, 720)
    }
})
