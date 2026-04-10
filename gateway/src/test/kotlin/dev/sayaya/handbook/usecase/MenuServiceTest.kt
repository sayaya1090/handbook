package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Menu
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeBetween
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration

class MenuServiceTest : DescribeSpec({
    val supplier1 = mockk<MenuSupplier>()
    val supplier2 = mockk<MenuSupplier>()
    val supplier3 = mockk<MenuSupplier>()
    val menuService = MenuService(listOf(supplier1, supplier2, supplier3))
    val headers = emptyMap<String, List<String>>()

    val menu1 = Menu.builder().title("메뉴1").order("1").build()
    val menu2 = Menu.builder().title("메뉴2").order("2").build()
    val menu3 = Menu.builder().title("메뉴3").order("3").build()

    beforeTest { clearMocks(supplier1, supplier2, supplier3) }

    describe("MenuService는") {

        it("여러 공급자의 메뉴를 병렬로 호출하여 순서대로 정렬한다") {
            every { supplier1.menu(headers) } returns Flux.just(menu2).delaySequence(Duration.ofMillis(700))
            every { supplier2.menu(headers) } returns Flux.just(menu1).delaySequence(Duration.ofMillis(1000))
            every { supplier3.menu(headers) } returns Flux.just(menu3).delaySequence(Duration.ofMillis(500))

            val startTime = System.currentTimeMillis()
            StepVerifier.create(menuService.menus(headers))
                .expectNext(menu1, menu2, menu3)
                .verifyComplete()
            val totalTime = System.currentTimeMillis() - startTime

            // 병렬 실행: ~1000ms (가장 긴 작업), 순차 실행이면 2200ms
            totalTime.shouldBeBetween(800, 1800)
            verify(exactly = 1) { supplier1.menu(headers) }
            verify(exactly = 1) { supplier2.menu(headers) }
            verify(exactly = 1) { supplier3.menu(headers) }
        }

        it("일부 공급자에서 에러가 발생해도 다른 공급자의 메뉴를 반환한다") {
            every { supplier1.menu(headers) } returns Flux.error(RuntimeException("실패"))
            every { supplier2.menu(headers) } returns Flux.just(menu1)
            every { supplier3.menu(headers) } returns Flux.just(menu3)

            StepVerifier.create(menuService.menus(headers))
                .expectNext(menu1, menu3)
                .verifyComplete()
        }

        it("모든 공급자가 빈 결과를 반환하면 빈 Flux를 반환한다") {
            every { supplier1.menu(headers) } returns Flux.empty()
            every { supplier2.menu(headers) } returns Flux.empty()
            every { supplier3.menu(headers) } returns Flux.empty()

            StepVerifier.create(menuService.menus(headers))
                .verifyComplete()
        }

        it("null order를 가진 메뉴는 마지막에 정렬된다") {
            val menuNoOrder = Menu.builder().title("순서없음").build()
            every { supplier1.menu(headers) } returns Flux.just(menu2, menuNoOrder)
            every { supplier2.menu(headers) } returns Flux.just(menu1)
            every { supplier3.menu(headers) } returns Flux.just(menu3)

            StepVerifier.create(menuService.menus(headers))
                .expectNext(menu1, menu2, menu3, menuNoOrder)
                .verifyComplete()
        }
    }
})
