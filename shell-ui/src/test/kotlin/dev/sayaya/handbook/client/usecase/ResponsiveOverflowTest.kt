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
    given("경계값 — 1px 초과 (overflow 트리거 즉시 진입)") {
        val r = ResponsiveOverflow.compute(300.0, 200.0, 101.0, 48.0)
        then("전체는 넘치지만 상단 200 + reserve 48 = 248 < 300 → overflow 만 활성, 스크롤 없음") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe false
        }
    }
    given("경계값 — top + reserve 가 container 와 정확히 일치") {
        val r = ResponsiveOverflow.compute(248.0, 200.0, 100.0, 48.0)
        then("2단계 조건(top + reserve <= container) 이 <= 로 포함이라 평면이 아닌 overflow 로 수렴 (경계 포함)") {
            // top+bottom=300 > 248 이라 1단계 실패, top+reserve=248 <= 248 이라 2단계 성공.
            r.showOverflow shouldBe true
            r.scrollable shouldBe false
        }
    }
    given("경계값 — top 이 container 보다 1px 초과") {
        val r = ResponsiveOverflow.compute(247.0, 200.0, 100.0, 48.0)
        then("2단계 실패 (top + reserve = 248 > 247) → 3단계 scrollable 진입") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe true
        }
    }
    given("극단값 — container 가 모든 입력보다 훨씬 작음") {
        val r = ResponsiveOverflow.compute(10.0, 200.0, 100.0, 48.0)
        then("전체 overflow + scrollable") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe true
        }
    }
    given("극단값 — container 가 0") {
        val r = ResponsiveOverflow.compute(0.0, 200.0, 100.0, 48.0)
        then("계산기는 crash 없이 overflow + scrollable 반환 (실제 컨테이너 레이아웃 전 상태 가정)") {
            r.showOverflow shouldBe true
            r.scrollable shouldBe true
        }
    }
    given("극단값 — 모든 입력 0") {
        val r = ResponsiveOverflow.compute(0.0, 0.0, 0.0, 0.0)
        then("아무것도 표시할 게 없으므로 overflow/scroll 모두 false") {
            r.showOverflow shouldBe false
            r.scrollable shouldBe false
        }
    }
})
