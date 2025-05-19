package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.LayoutService
import dev.sayaya.handbook.usecase.TypeService
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher")
@ExtendWith(SpringExtension::class)
internal class LayoutControllerTest : ShouldSpec({
    val mockService = mockk<LayoutService>()
    val controller = LayoutController(mockService)
    val webTestClient = WebTestClient.bindToController(controller).build()
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    val later = now.plusSeconds(3600)

    beforeEach {
        clearMocks(mockService, recordedCalls = true, answers = false, verificationMarks = true)
    }

    context("layouts 엔드포인트 테스트") {
        should("올바른 layouts 요청 시 레이아웃 목록을 반환해야 한다") {
            // Given: Mock된 서비스 응답 정의 (새로운 Layout 구조 사용)
            val layout1EffectTime = now.minusSeconds(7200)
            val layout1ExpireTime = now.minusSeconds(3600)
            val layout2EffectTime = now
            val layout2ExpireTime = later

            val expectedLayouts = listOf(
                Layout(
                    workspace = workspace,
                    effectDateTime = layout1EffectTime,
                    expireDateTime = layout1ExpireTime
                ), Layout(
                    workspace = workspace,
                    effectDateTime = layout2EffectTime,
                    expireDateTime = layout2ExpireTime
                )
            )
            every { mockService.findAll(workspace) } returns Flux.fromIterable(expectedLayouts)

            // When: API 호출
            webTestClient.get().uri("/workspace/$workspace/layouts")
                .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .exchange()
                // Then: 응답 검증 (새로운 Layout 필드 검증)
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .expectBody(object : ParameterizedTypeReference<List<Layout>>() {})
                .isEqualTo(expectedLayouts)
        }
        should("layouts 요청 시 서비스가 빈 목록을 반환하면 빈 배열을 반환해야 한다") {
            // Given: Mock된 서비스가 빈 Flux 반환
            every { mockService.findAll(workspace) } returns Flux.empty()

            // When: API 호출
            webTestClient.get().uri("/workspace/$workspace/layouts")
                .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .exchange()
                // Then: 응답 검증
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .expectBody(object : ParameterizedTypeReference<List<Layout>>() {})
                .isEqualTo(emptyList())
        }
    }
})
