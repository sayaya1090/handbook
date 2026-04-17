package dev.sayaya.handbook.interfaces.discovery

import dev.sayaya.handbook.domain.Menu
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.TimeoutException

class ServiceDiscoveryTest : DescribeSpec({
    val mockWebClient = mockk<WebClient>()
    val mockWebClientBuilder = mockk<WebClient.Builder>()
    val mockResponseSpec = mockk<WebClient.ResponseSpec>()

    val serviceName = "test-service"
    val testMenu = Menu.builder().title("test").build()

    every { mockWebClientBuilder.baseUrl("http://$serviceName") } returns mockWebClientBuilder
    every { mockWebClientBuilder.build() } returns mockWebClient

    val discovery = ServiceDiscovery(mockWebClientBuilder, serviceName)

    beforeTest {
        every { mockWebClient.get().uri("/menus").headers(any()).accept(any()).retrieve() } returns mockResponseSpec
    }

    describe("ServiceDiscovery는") {
        it("서비스로부터 메뉴를 조회한다") {
            every { mockResponseSpec.bodyToFlux(Menu::class.java) } returns Flux.just(testMenu)

            val result = discovery.menu(emptyMap()).collectList().block()
            result!!.size shouldBe 1
            result[0] shouldBe testMenu
        }

        it("타임아웃 초과 시 예외를 발생시킨다") {
            every { mockResponseSpec.bodyToFlux(Menu::class.java) } returns
                Flux.just(testMenu).delayElements(Duration.ofMillis(2000))

            val ex = shouldThrow<RuntimeException> {
                discovery.menu(emptyMap()).blockFirst(Duration.ofMillis(2000))
            }
            ex.cause.shouldBeInstanceOf<TimeoutException>()
        }
    }
})
