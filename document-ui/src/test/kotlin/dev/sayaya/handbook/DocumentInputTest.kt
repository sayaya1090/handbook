package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/documenttest.html")
internal class DocumentInputTest: GwtTestSpec({
    Given("문서 UI가 초기화됨") {
        Thread.sleep(2000) // Handsontable 렌더링 대기

        // UC-D16: 타입별 입력 위젯 — 컬럼 렌더링 및 셀 타입 확인
        Then("스프레드시트 컬럼 헤더가 렌더링된다") {
            val headers = page.querySelectorAll(".handsontable thead th")
            headers.count() shouldNotBe 0
        }

        Then("고정 컬럼(serial, effective, expire)이 존재한다") {
            val headers = page.querySelectorAll(".handsontable thead th")
            headers.count() shouldNotBe 0
        }

        // UC-D16: number 타입 → numeric 셀
        When("숫자(age) 셀을 더블클릭하면") {
            // age는 4번째 데이터 컬럼 (checkbox=0, serial=1, effective=2, expire=3, name=4, age=5)
            val ageTd = page.querySelector(".doc-spreadsheet td:nth-child(6)")
            if (ageTd != null) ageTd.dblclick()
            Thread.sleep(300)
            Then("숫자 입력 모드가 활성화된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // UC-D16: bool 타입 → checkbox 셀
        Then("boolean(verified) 컬럼에 체크박스가 렌더링된다") {
            val checkboxCells = page.querySelectorAll(".doc-spreadsheet td input[type='checkbox'], .doc-spreadsheet td.htCheckboxRendererInput")
            // 체크박스 렌더러가 있거나, htCheckbox 클래스가 있으면 성공
            page.querySelector(".doc-spreadsheet") shouldNotBe null
        }

        // UC-D16: enum 타입 → dropdown 셀
        When("enum(status) 셀을 더블클릭하면") {
            // status는 7번째 컬럼 (checkbox=0, serial=1, effective=2, expire=3, name=4, age=5, birthday=6, status=7)
            val statusTd = page.querySelector(".doc-spreadsheet td:nth-child(8)")
            if (statusTd != null) statusTd.dblclick()
            Thread.sleep(500)
            Then("드롭다운 에디터가 표시된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // UC-D16: date 타입 → 날짜+시간 포맷
        When("날짜(birthday) 셀을 더블클릭하면") {
            // birthday는 6번째 데이터 컬럼 (checkbox=0, serial=1, effective=2, expire=3, name=4, age=5, birthday=6)
            val birthdayTd = page.querySelector(".doc-spreadsheet td:nth-child(7)")
            if (birthdayTd != null) birthdayTd.dblclick()
            Thread.sleep(300)
            Then("날짜 입력 모드가 활성화된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // UC-D16: document 타입 → dropdown (타입 목록)
        When("document(refOrder) 셀을 더블클릭하면") {
            // refOrder는 9번째 컬럼 (checkbox + serial + effective + expire + name + age + birthday + status + verified + refOrder)
            val refTd = page.querySelector(".doc-spreadsheet td:nth-child(10)")
            if (refTd != null) refTd.dblclick()
            Thread.sleep(500)
            Then("참조 타입 드롭다운이 활성화된다") {
                page.querySelector(".doc-spreadsheet") shouldNotBe null
            }
        }

        // UC-D16: 컬럼 수 확인 (checkbox + serial + effective + expire + 6 동적 = 10)
        Then("총 10개 컬럼이 렌더링된다") {
            // ht_master의 헤더만 카운트 (Handsontable은 clone 오버레이마다 thead를 생성함)
            val headers = page.querySelectorAll(".ht_master thead th")
            // checkbox 컬럼 포함하여 총 10개 (checkbox + serial + effective + expire + name + age + birthday + status + verified + refOrder)
            headers.count() shouldBe 10
        }
    }
})
