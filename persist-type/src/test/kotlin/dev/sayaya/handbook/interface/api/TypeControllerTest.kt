package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.usecase.type.TypeService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Suppress("ReactiveStreamsUnusedPublisher")
@WebFluxTest
@ContextConfiguration(classes = [ TypeController::class, JsonConfig::class, TypeControllerTest.Companion.SecurityConfig::class ])
internal class TypeControllerTest(
    private val webTestClient: WebTestClient,
    private val mockService: TypeService
) : ShouldSpec({
    val client = webTestClient.mutateWith(mockUser()).mutateWith(csrf())
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
    val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    val later = now.plusSeconds(3600)

    val expected = listOf(
        Type("type_1", "t1-v2", now, later, "type_1", true, emptyList(), null,
            100u, 100u, 200u, 200u),
        Type("type_2", "t2-v2", now, later, "type_2", true, emptyList(), null,
            200u, 200u, 300u, 300u)
    )
    beforeEach {
        clearMocks(mockService, recordedCalls = true, answers = false, verificationMarks = true)
    }

    context("types 엔드포인트 테스트") {
        should("올바른 저장 요청 시 서비스의 저장 함수를 호출하고 결과를 반환해야 한다") {
            // Given: Mock된 서비스 응답 정의
            every { mockService.save(any(), any(), any()) } returns expected.toMono()
            // When: API 호출
            client.put().uri { builder ->
                builder.path("/workspace/$workspace/types").build()
            }.contentType(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
                .bodyValue("""[
                    {"id":"type_2","version":"t2-v2","effect_date_time":946598400000,"expire_date_time":32503593600000,"description":"type_2","primitive":true,"attributes":[],"parent":"type_1","x":817,"y":136,"width":200,"height":200}
                ]""".trimIndent())
                .exchange()
                .expectStatus().isOk
                .expectBody().isEmpty

            verify(exactly = 1) { mockService.save(any(), workspace, any()) }
        }
    }
}) {
    companion object {
        @TestConfiguration
        @EnableWebFluxSecurity
        class SecurityConfig {
            @Bean fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
                anonymous { }
            }
            @Bean fun typeService(): TypeService = mockk()
        }
    }
}