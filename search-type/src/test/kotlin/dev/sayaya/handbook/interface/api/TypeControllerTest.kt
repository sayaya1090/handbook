package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Layout
import dev.sayaya.handbook.domain.Type
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
internal class TypeControllerTest : ShouldSpec({
    val mockService = mockk<TypeService>()
    val controller = TypeController(mockService)
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
    context("types 엔드포인트 테스트") {
        val effectDateTime = now.minus(1, ChronoUnit.HOURS) // 검색 시작 시간
        val expireDateTime = now.plus(1, ChronoUnit.HOURS)   // 검색 종료 시간

        should("올바른 범위 검색 요청 시 기간에 맞는 Type 목록을 반환해야 한다") {
            // Given: Mock된 서비스 응답 정의
            val expectedTypes = listOf(
                Type(
                    id = "type_1",
                    parent = null,
                    version = "v1",
                    effectDateTime = effectDateTime.minusSeconds(100),
                    expireDateTime = effectDateTime.plusSeconds(100),
                    description = "description",
                    primitive = true,
                    attributes = emptyList(),
                    x = 0u, y = 100u, width = 100u, height = 80u
                ), Type(
                    id = "type_2",
                    parent = null,
                    version = "v2",
                    effectDateTime = effectDateTime.minusSeconds(100),
                    expireDateTime = effectDateTime.plusSeconds(100),
                    description = "description",
                    primitive = true,
                    attributes = emptyList(),
                    x = 150u, y = 100u, width = 80u, height = 100u
                )
            )
            every { mockService.findByRange(workspace,effectDateTime, expireDateTime) } returns Flux.fromIterable(expectedTypes)

            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("effect_date_time", effectDateTime.toString())
                    .queryParam("expire_date_time", expireDateTime.toString())
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
                .expectStatus().isOk
                .expectBody(object : ParameterizedTypeReference<List<Type>>() {})
                .isEqualTo(expectedTypes)

            verify(exactly = 1) { mockService.findByRange(workspace, effectDateTime, expireDateTime) }
        }

        should("effect_date_time가 누락되면 400 BAD_REQUEST를 반환해야 한다") {
            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
                .expectStatus().isBadRequest
        }
        should("expire_date_time이 null일 경우 effect_date_time과 동일한 값으로 동작해야 한다") {
            // Given: Mock된 서비스 응답 정의
            val expectedTypes = listOf(
                Type(
                    id = "type_1",
                    parent = null,
                    version = "v1",
                    effectDateTime = effectDateTime,
                    expireDateTime = effectDateTime.plusSeconds(100),
                    description = "description",
                    primitive = true,
                    attributes = emptyList(),
                    x = 0u, y = 100u, width = 100u, height = 80u
                )
            )
            every { mockService.findByRange(workspace, effectDateTime, effectDateTime) } returns Flux.fromIterable(expectedTypes)

            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("effect_date_time", effectDateTime.toString())
                    .build() // expire_date_time 누락
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
                // Then: 서비스 호출 및 반환 값 확인
                .expectStatus().isOk
                .expectBody(object : ParameterizedTypeReference<List<Type>>() {})
                .isEqualTo(expectedTypes)

            // Verify: 서비스 메소드 호출이 올바른 값으로 수행되었는지 확인
            verify(exactly = 1) { mockService.findByRange(workspace, effectDateTime, effectDateTime) }
        }

        should("날짜/시간 파라미터 형식이 잘못되면 400 BAD_REQUEST를 반환해야 한다") {
            // When: 잘못된 형식의 expire_date_time 파라미터로 API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("effect_date_time", effectDateTime.toString())
                    .queryParam("expire_date_time", "invalid-date-time-format") // 잘못된 형식
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .exchange()
                // Then: 400 Bad Request 확인
                .expectStatus().isBadRequest
                .expectBody(String::class.java) // 에러 메시지 본문 확인 (선택 사항)
                .value { body ->
                    body shouldContain "invalid-date-time-format" // 잘못된 값이 포함되는지 확인
                }
        }

        should("서비스에서 에러가 발생하면 500 Internal Server Error를 반환해야 한다") {
            // Given: 서비스 호출 시 에러 발생하도록 Mock 설정
            val exception = RuntimeException("Internal service error")
            every { mockService.findByRange(workspace, effectDateTime, expireDateTime) } returns Flux.error(exception)

            // When: API 호출
            webTestClient.get().uri { builder ->
                builder.path("/workspace/$workspace/types")
                    .queryParam("effect_date_time", effectDateTime.toString())
                    .queryParam("expire_date_time", expireDateTime.toString())
                    .build()
            }.accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .exchange()
                // Then: 500 Internal Server Error 확인 (컨트롤러의 @ExceptionHandler 때문에 400으로 변환될 수도 있음 - 확인 필요)
                // 현재 컨트롤러는 IllegalArgumentException 만 400으로 처리하므로, RuntimeException은 500이 될 가능성이 높음
                .expectStatus().isEqualTo(500) // 또는 컨트롤러 ExceptionHandler 동작에 따라 .isBadRequest()

            // Verify: 서비스 메소드 호출 확인
            verify(exactly = 1) { mockService.findByRange(workspace, effectDateTime, expireDateTime) }
        }
    }
})
