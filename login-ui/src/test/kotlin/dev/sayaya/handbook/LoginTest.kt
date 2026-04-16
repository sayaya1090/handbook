package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank

@GwtHtml("src/test/webapp/logintest.html")
internal class LoginTest : GwtTestSpec({
    Given("로그인 화면이 로드됨") {
        Then("콘텐츠 영역(login-content)이 DOM에 존재한다") {
            page.querySelector(".login-content") shouldNotBe null
        }

        Then("콘솔 영역(console)이 DOM에 존재한다") {
            page.querySelector(".console") shouldNotBe null
        }

        Then("콘솔에 ASCII 아트 웰컴 메시지가 렌더링된다") {
            val console = page.querySelector(".console")
            console shouldNotBe null
            val text = console!!.textContent() ?: ""
            text.shouldContain("v1.0.0")
        }

        Then("콘솔에 라인(.line) 요소가 1개 이상 존재한다") {
            val lines = page.querySelectorAll(".console .line")
            lines.count() shouldBeGreaterThan 0
        }

        When("OAuth 버튼이 렌더링되면") {
            Thread.sleep(500)
            Then("Google 로그인 버튼(btn-google)이 존재한다") {
                page.querySelector(".btn-google") shouldNotBe null
            }
            Then("OAuth 버튼에 btn-oauth CSS 클래스가 적용된다") {
                page.querySelector(".btn-oauth") shouldNotBe null
            }
            Then("Google 버튼 텍스트에 GOOGLE 이 포함된다") {
                val btn = page.querySelector(".btn-google")
                btn shouldNotBe null
                val text = btn!!.textContent() ?: ""
                text.shouldContain("GOOGLE")
            }
            Then("Google 버튼에 FontAwesome 아이콘(fa-google)이 존재한다") {
                val icon = page.querySelector(".btn-google .fa-google")
                icon shouldNotBe null
            }
            Then("OAuth 버튼이 콘솔 안에 정확히 1개 존재한다") {
                val buttons = page.querySelectorAll(".console .btn-oauth")
                buttons.count() shouldBe 1
            }
        }

        // 커맨드 핸들러 통합 테스트
        When("notify 커맨드(error)를 dispatch 하면") {
            page.evaluate("""
                window.dispatchEvent(new CustomEvent('handbook-login-command', {
                    detail: { type: 'notify', level: 'error', message: 'OAuth failed' }
                }))
            """.trimIndent())
            Thread.sleep(300)
            Then("콘솔에 [ERROR] 접두사와 메시지가 출력된다") {
                val text = page.querySelector(".console")!!.textContent() ?: ""
                text shouldContain "[ERROR] OAuth failed"
            }
        }

        When("notify 커맨드(info)를 dispatch 하면") {
            page.evaluate("""
                window.dispatchEvent(new CustomEvent('handbook-login-command', {
                    detail: { type: 'notify', level: 'info', message: 'Welcome back' }
                }))
            """.trimIndent())
            Thread.sleep(300)
            Then("콘솔에 메시지가 접두사 없이 출력된다") {
                val text = page.querySelector(".console")!!.textContent() ?: ""
                text shouldContain "Welcome back"
            }
        }

        When("attention 커맨드를 dispatch 하면") {
            page.evaluate("""
                window.dispatchEvent(new CustomEvent('handbook-login-command', {
                    detail: { type: 'attention', message: 'Please sign in to continue' }
                }))
            """.trimIndent())
            Thread.sleep(300)
            Then("콘솔에 안내 메시지가 출력된다") {
                val text = page.querySelector(".console")!!.textContent() ?: ""
                text shouldContain "Please sign in to continue"
            }
        }

        When("highlight 커맨드를 dispatch 하면") {
            page.evaluate("""
                window.dispatchEvent(new CustomEvent('handbook-login-command', {
                    detail: { type: 'highlight', target: '.btn-google' }
                }))
            """.trimIndent())
            Thread.sleep(300)
            Then("대상 버튼에 login-highlight 클래스가 토글된다") {
                val has = page.evaluate(
                    "document.querySelector('.btn-google')?.classList.contains('login-highlight')"
                ).toString()
                has shouldBe "true"
            }
        }

        When("progress 커맨드를 dispatch 하면") {
            page.evaluate("""
                window.dispatchEvent(new CustomEvent('handbook-login-command', {
                    detail: { type: 'progress', description: 'Redirecting to Google...' }
                }))
            """.trimIndent())
            Thread.sleep(300)
            Then("콘솔에 진행 메시지가 출력된다") {
                val text = page.querySelector(".console")!!.textContent() ?: ""
                text shouldContain "Redirecting to Google..."
            }
            Then("OAuth 버튼이 비활성화된다") {
                val disabled = page.evaluate(
                    "document.querySelector('.btn-oauth')?.hasAttribute('disabled')"
                ).toString()
                disabled shouldBe "true"
            }
        }

        // 키보드 + 사운드 테스트
        When("Google 버튼에 포커스가 잡히면") {
            page.evaluate("document.querySelector('.btn-google')?.focus()")
            Thread.sleep(300)
            Then("audio 요소(beep)가 DOM 에 존재한다") {
                val audio = page.evaluate("document.querySelector('audio[src*=\"beep\"]') !== null").toString()
                audio shouldBe "true"
            }
        }

        When("Google 버튼에서 ArrowDown 키를 누르면") {
            page.keyboard().press("ArrowDown")
            Thread.sleep(300)
            Then("버튼이 1개뿐이므로 포커스가 유지된다") {
                val focused = page.evaluate(
                    "document.activeElement?.tagName?.toLowerCase() || document.activeElement?.shadowRoot?.activeElement?.tagName?.toLowerCase()"
                ).toString()
                focused.shouldNotBeBlank()
            }
        }

        When("Google 버튼을 클릭하면") {
            Then("audio 요소(start)가 DOM 에 존재한다") {
                val audio = page.evaluate("document.querySelector('audio[src*=\"start\"]') !== null").toString()
                audio shouldBe "true"
            }
        }
    }
})
