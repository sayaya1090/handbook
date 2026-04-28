package dev.sayaya.handbook.usecase

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import io.kotest.matchers.shouldBe

@GwtHtml("activityTest.html")
class ActivityTest : GwtTestSpec({
    Given("Activity Module Runtime Scenarios") {
        Then("모든 유틸리티 및 도메인 로직이 정상 실행되어야 한다") {
            page shouldContainLog "LOG_PREF_LANG:ko-KR"
            page shouldContainLog "LOG_PREF_THEME:dark"
            page shouldContainLog "LOG_DETECTED_LANG:ko"
            page shouldContainLog "LOG_MENU_AUTH_ALLOWED:true"
            page shouldContainLog "LOG_VIEWPORT_MOBILE:false"
            page shouldContainLog "LOG_FETCH_SUCCESS"
        }
        
        Then("브릿지 통신이 정상 작동해야 한다") {
            page shouldContainLog "LOG_TOOL_PUBLISHED:tool-1"
            page shouldContainLog "LOG_TOOL_SELECTED:selected-id"
            page shouldContainLog "LOG_RENDER_RECEIVED:true"
            page shouldContainLog "LOG_PROGRESS_RECEIVED:working-state"
            page shouldContainLog "LOG_URI_RECEIVED:/test/path"
        }

        Then("화면 크기 변경에 반응해야 한다") {
            page.setViewportSize(375, 812)
            Thread.sleep(300)
            page shouldContainLog "LOG_VIEWPORT_MOBILE:true"
            
            page.setViewportSize(1280, 800)
            Thread.sleep(300)
            // 마지막 상태 확인
            page shouldContainLog "ACTIVITY_TEST_READY"
        }
    }
})
