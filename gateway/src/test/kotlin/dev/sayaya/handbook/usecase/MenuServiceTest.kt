package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.client.domain.Menu
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.longs.shouldBeBetween
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpHeaders
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Duration

@Suppress("ReactiveStreamsUnusedPublisher")
internal class MenuServiceTest : ShouldSpec({
    val supplier1 = mockk<MenuSupplier>()
    val supplier2 = mockk<MenuSupplier>()
    val supplier3 = mockk<MenuSupplier>()
    val suppliers = listOf(supplier1, supplier2, supplier3)
    val menuService = MenuService(suppliers)
    val headers = HttpHeaders()
    val menu1 = Menu.builder().order("1").build()
    val menu2 = Menu.builder().order("2").build()
    val menu3 = Menu.builder().order("3").build()

    beforeTest {
        clearMocks(supplier1, supplier2, supplier3)
    }

    should("여러 공급자의 메뉴를 병렬로 호출하여 병합하고 순서대로 정렬해야 함") {
        // Given
        every { supplier1.menu(headers) } returns Flux.just(menu2).delaySequence(Duration.ofMillis(700))
        every { supplier2.menu(headers) } returns Flux.just(menu1).delaySequence(Duration.ofMillis(1000))
        every { supplier3.menu(headers) } returns Flux.just(menu3).delaySequence(Duration.ofMillis(500))

        val startTime = System.currentTimeMillis()
        // When & Then
        menuService.menus(headers)
            .let(StepVerifier::create)
            .expectNext(menu1)
            .expectNext(menu2)
            .expectNext(menu3)
            .verifyComplete()
        val totalTime = System.currentTimeMillis() - startTime

        verify(exactly = 1) { supplier1.menu(headers) }
        verify(exactly = 1) { supplier2.menu(headers) }
        verify(exactly = 1) { supplier3.menu(headers) }

        // 순차 실행 시 예상 시간: 500 + 700 + 1000 = 2200ms
        // 병렬 실행 시 예상 시간: ~1000ms (가장 긴 작업 시간)
        // 허용 오차를 감안해 1500ms 미만이면 병렬 실행으로 간주
        totalTime.shouldBeBetween(1000, 1500)
    }

    should("일부 공급자에서 에러가 발생해도 다른 공급자의 메뉴를 반환해야 함") {
        // Given
        every { supplier1.menu(headers) } returns Flux.error<Menu>(RuntimeException("메뉴 조회 실패")).delaySequence(Duration.ofMillis(700))
        every { supplier2.menu(headers) } returns Flux.just(menu1).delaySequence(Duration.ofMillis(1000))
        every { supplier3.menu(headers) } returns Flux.just(menu3).delaySequence(Duration.ofMillis(500))

        // When & Then
        menuService.menus(headers)
            .let(StepVerifier::create)
            .expectNext(menu1)
            .expectNext(menu3)
            .verifyComplete()

        verify(exactly = 1) { supplier1.menu(headers) }
        verify(exactly = 1) { supplier2.menu(headers) }
        verify(exactly = 1) { supplier3.menu(headers) }
    }

    should("모든 공급자가 빈 결과를 반환하면 빈 Flux를 반환해야 함") {
        // Given
        every { supplier1.menu(headers) } returns Flux.empty<Menu>().delaySequence(Duration.ofMillis(700))
        every { supplier2.menu(headers) } returns Flux.empty<Menu>().delaySequence(Duration.ofMillis(1000))
        every { supplier3.menu(headers) } returns Flux.empty<Menu>().delaySequence(Duration.ofMillis(500))

        // When & Then
        val result = menuService.menus(headers)

        StepVerifier.create(result).verifyComplete()

        verify(exactly = 1) { supplier1.menu(headers) }
        verify(exactly = 1) { supplier2.menu(headers) }
        verify(exactly = 1) { supplier3.menu(headers) }
    }

    should("null order를 가진 메뉴는 마지막에 정렬되어야 함") {
        // Given
        val menuWithNullOrder = mockk<Menu>()
        every { menuWithNullOrder.order() } returns null

        every { supplier1.menu(headers) } returns Flux.just(menu2, menuWithNullOrder)
        every { supplier2.menu(headers) } returns Flux.just(menu1)
        every { supplier3.menu(headers) } returns Flux.just(menu3)

        // When & Then
        menuService.menus(headers)
            .let(StepVerifier::create)
            .expectNext(menu1)
            .expectNext(menu2)
            .expectNext(menu3)
            .expectNext(menuWithNullOrder)
            .verifyComplete()

        verify(exactly = 1) { supplier1.menu(headers) }
        verify(exactly = 1) { supplier2.menu(headers) }
        verify(exactly = 1) { supplier3.menu(headers) }
    }
})