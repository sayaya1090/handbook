package dev.sayaya.handbook.client.usecase

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ResponsiveOverflowTest : BehaviorSpec({
    given("상단정렬·하단정렬 합계가 컨테이너 안에 들어감") {
        val r = ResponsiveOverflow.compute(500.0, 200.0, 100.0, 48.0)
        `when`("평면 렌더") {
            then("overflow 버튼도 스크롤도 필요 없다") {
                r.showOverflow shouldBe false
                r.scrollable shouldBe false
            }
        }
    }
    given("전체는 넘치지만 상단정렬 + overflow 예약 폭은 들어감") {
        val r = ResponsiveOverflow.compute(300.0, 200.0, 200.0, 48.0)
        then("하단정렬만 overflow 로 이동, 상단정렬은 평면 유지") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe false
        }
    }
    given("상단정렬만으로도 컨테이너를 넘침") {
        val r = ResponsiveOverflow.compute(150.0, 200.0, 100.0, 48.0)
        then("overflow 버튼과 스크롤이 모두 활성화") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe true
        }
    }
    given("하단정렬 공급자가 0개인 경우") {
        `when`("상단정렬이 컨테이너에 들어가면") {
            val r = ResponsiveOverflow.compute(400.0, 200.0, 0.0, 48.0)
            then("overflow 도 스크롤도 필요 없다") {
                r.showOverflow shouldBe false
                r.scrollable shouldBe false
            }
        }
        `when`("상단정렬이 컨테이너를 넘치면") {
            val r = ResponsiveOverflow.compute(150.0, 200.0, 0.0, 48.0)
            then("overflow 버튼은 뜨지 않고 스크롤만 활성") {
                r.showOverflow shouldBe false
                r.scrollable shouldBe true
            }
        }
    }
    given("경계값 — 정확히 컨테이너 폭과 일치") {
        val r = ResponsiveOverflow.compute(300.0, 200.0, 100.0, 48.0)
        then("딱 맞아떨어지면 평면") {
            r.showOverflow shouldBe false
            r.scrollable shouldBe false
        }
    }
})
