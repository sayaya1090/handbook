package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("documenttest.html")
internal class DocumentMobileTest: GwtTestSpec({
    Given("문서 UI가 모바일 뷰포트에서 초기화됨") {
        page.setViewportSize(375, 667) // iPhone SE

        // UC-D10: 모바일 반응형 레이아웃
        Then("컨트롤러 툴바가 존재한다") {
            page.querySelector(".doc-controller") shouldNotBe null
        }
        Then("타입 탭이 수평 스크롤 가능하다") {
            val tabs = page.querySelector(".doc-type-tabs")
            tabs shouldNotBe null
            // @media (max-width: 768px)에서 overflow-x: auto 적용 확인
            val overflow = tabs!!.evaluate("el => getComputedStyle(el).overflowX") as String
            overflow shouldBe "auto"
        }
        Then("버튼의 터치 영역이 44px 이상이다") {
            val btn = page.querySelector(".doc-ctrl-btn-add")
            btn shouldNotBe null
            val height = btn!!.evaluate("el => el.offsetHeight") as Number
            (height.toInt() >= 44) shouldBe true
        }
    }
})
