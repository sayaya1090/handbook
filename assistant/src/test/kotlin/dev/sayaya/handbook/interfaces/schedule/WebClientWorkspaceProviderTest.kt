package dev.sayaya.handbook.interfaces.schedule

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.util.*

class WebClientWorkspaceProviderTest : DescribeSpec({
    val webClient = mockk<WebClient>()
    val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
    val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
    val responseSpec = mockk<WebClient.ResponseSpec>()
    val provider = WebClientWorkspaceProvider(webClient)

    describe("WebClientWorkspaceProvider") {
        it("getActiveWorkspaces: 활성 워크스페이스 목록을 조회한다") {
            val id = UUID.randomUUID()
            every { webClient.get() } returns requestHeadersUriSpec
            every { requestHeadersUriSpec.uri("/workspaces/active") } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToFlux(UUID::class.java) } returns Flux.just(id)

            val result = provider.getActiveWorkspaces()

            result shouldBe listOf(id)
        }
        it("에러 발생 시 빈 목록을 반환한다") {
            every { webClient.get() } throws RuntimeException("API error")
            val result = provider.getActiveWorkspaces()
            result shouldBe emptyList()
        }
    }
})
