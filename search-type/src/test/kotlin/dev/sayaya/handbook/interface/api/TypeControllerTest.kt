package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.LayoutService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
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
internal class TypeControllerTest : ShouldSpec({
    val mockService = mockk<LayoutService>()
    val controller = TypeController(mockService)
    val webTestClient = WebTestClient.bindToController(controller).build()
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    val later = now.plusSeconds(3600)

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
    context("types 엔드포인트 테스트") {
        should("올바른 검색 요청 시 올바른 결과를 반환해야 한다") {
            // Given: Mock된 서비스 응답 정의
            val baseTime = Instant.now()
            val expectedTypes = listOf(
                TypeWithLayout(
                    type = Type(
                        id = "type_1",
                        parent = null,
                        version = "v1",
                        effectDateTime = Instant.now(),
                        expireDateTime = Instant.now().plusSeconds(3600),
                        description = "description",
                        primitive = true,
                        attributes = emptyList()
                    ), x = 0u, y = 100u, width = 1u, height = 2u
                )
            )
            every { mockService.findByBaseTime(workspace, any()) } returns Flux.fromIterable(expectedTypes)

            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("basetime", baseTime.toString())
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$[0].type.id").isEqualTo("type_1")
                .jsonPath("$[0].type.version").isEqualTo("v1")
                .jsonPath("$[0].x").isEqualTo(0u)
                .jsonPath("$[0].y").isEqualTo(100u)
                .jsonPath("$[0].width").isEqualTo(1u)
                .jsonPath("$[0].height").isEqualTo(2u)
        }
        should("잘못된 요청이 들어오면 400 BAD_REQUEST를 반환해야 한다") {
            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("basetime", "invalid basetime")
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .consumeWith { response ->
                    assert(response.responseBody!!.toString(Charsets.UTF_8) == "Parse attempt failed for value [invalid basetime]")
                }
        }
    }

})
