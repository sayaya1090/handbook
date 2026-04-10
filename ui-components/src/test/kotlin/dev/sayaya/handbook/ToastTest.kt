package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/uicomponentstest.html")
internal class ToastTest: GwtTestSpec({
    Given("UI 컴포넌트가 초기화됨") {
        Then("토스트 컨테이너가 존재한다") {
            page.querySelector(".ui-toast-container") shouldNotBe null
        }

        // INFO 토스트
        When("INFO 토스트 버튼을 클릭하면") {
            page.click("#btn-toast-info")
            Thread.sleep(500)

            Then("INFO 토스트가 표시된다") {
                val toast = page.querySelector(".ui-toast-info")
                toast shouldNotBe null
            }

            Then("토스트에 ui-toast 기본 클래스가 적용된다") {
                val toast = page.querySelector(".ui-toast-info")
                val classes = toast!!.getAttribute("class") ?: ""
                classes.contains("ui-toast") shouldBe true
            }

            Then("토스트 메시지 텍스트가 포함된다") {
                val toast = page.querySelector(".ui-toast-info")
                toast!!.textContent()!!.contains("정보 메시지입니다.") shouldBe true
            }

            Then("닫기 버튼이 존재한다") {
                val closeBtn = page.querySelector(".ui-toast-info .ui-toast-close")
                closeBtn shouldNotBe null
            }

            Then("3초 후 페이드아웃되거나 제거된다") {
                // 토스트는 3초 후 fadeout 클래스 추가, 그 300ms 후 DOM에서 제거됨
                // 테스트 시점에 따라 fadeout 상태이거나 이미 제거될 수 있음
                Thread.sleep(3500)
                val toast = page.querySelector(".ui-toast-info")
                val fadeout = page.querySelector(".ui-toast-fadeout")
                // 토스트가 이미 제거되었거나 fadeout 중이어야 함
                (toast == null || fadeout != null) shouldBe true
            }

            Then("페이드아웃 후 토스트가 제거된다") {
                Thread.sleep(500)
                val remaining = page.querySelectorAll(".ui-toast-info")
                remaining.count() shouldBe 0
            }
        }

        // ERROR 토스트
        When("ERROR 토스트 버튼을 클릭하면") {
            page.click("#btn-toast-error")
            Thread.sleep(500)

            Then("ERROR 토스트가 표시된다") {
                val toast = page.querySelector(".ui-toast-error")
                toast shouldNotBe null
            }

            Then("ERROR 토스트에 ui-toast-error 클래스가 적용된다") {
                val toast = page.querySelector(".ui-toast-error")
                val classes = toast!!.getAttribute("class") ?: ""
                classes.contains("ui-toast-error") shouldBe true
            }

            Then("ERROR 토스트는 3초 후에도 자동 닫히지 않는다") {
                Thread.sleep(3500)
                val toast = page.querySelector(".ui-toast-error")
                toast shouldNotBe null
            }

            Then("닫기 버튼을 클릭하면 토스트가 제거된다") {
                page.click(".ui-toast-error .ui-toast-close")
                Thread.sleep(500)
                val remaining = page.querySelectorAll(".ui-toast-error")
                remaining.count() shouldBe 0
            }
        }

        // 여러 토스트 동시 표시
        When("여러 토스트를 연속으로 표시하면") {
            page.click("#btn-toast-info")
            Thread.sleep(100)
            page.click("#btn-toast-error")
            Thread.sleep(500)

            Then("토스트 컨테이너에 2개 이상의 토스트가 존재한다") {
                val toasts = page.querySelectorAll(".ui-toast")
                toasts.count() shouldBe 2
            }
        }
    }
})
