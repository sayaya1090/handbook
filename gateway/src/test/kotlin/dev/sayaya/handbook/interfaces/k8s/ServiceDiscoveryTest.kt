package dev.sayaya.handbook.interfaces.k8s

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.`interface`.k8s.ServiceDiscovery
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.Duration

internal class ServiceDiscoveryTest : ShouldSpec({
    val mockWebClient = mockk<WebClient>()
    val mockWebClientBuilder = mockk<WebClient.Builder>()
    val mockResponseSpec = mockk<WebClient.ResponseSpec>()

    val serviceUrl = "test-service"
    val testHeaders = HttpHeaders()
    val testMenu = Menu()
    val testMenuList = listOf(testMenu)

    every { mockWebClientBuilder.baseUrl("http://$serviceUrl") } returns mockWebClientBuilder
    every { mockWebClientBuilder.build() } returns mockWebClient

    // ServiceDiscovery 인스턴스 생성
    val serviceDiscovery = ServiceDiscovery(mockWebClientBuilder, serviceUrl)

    // Mock 동작 설정
    beforeTest {
        every { mockWebClient.get().uri("/menus").headers(any()).accept(any()).retrieve() } returns mockResponseSpec
        every { mockResponseSpec.bodyToFlux(Menu::class.java) } returns Flux.fromIterable(testMenuList)
    }

    context("ServiceDiscovery") {
        should("정의된 메뉴를 성공적으로 반환한다") {
            // When
            val result = serviceDiscovery.menu(testHeaders).collectList().block()

            // Then
            result!!.size shouldBe testMenuList.size
            result[0] shouldBe testMenu
        }

        should("실행 시간이 500ms를 초과할 경우 IllegalStateException을 던진다") {
            // Given 타임아웃 발생 조건 설정
            every { mockResponseSpec.bodyToFlux(Menu::class.java) } returns Flux.just(testMenu).delayElements(Duration.ofMillis(1000))

            // When & Then
            shouldThrow<IllegalStateException> {
                serviceDiscovery.menu(testHeaders).blockFirst(Duration.ofMillis(500))
            }
        }

    }
})