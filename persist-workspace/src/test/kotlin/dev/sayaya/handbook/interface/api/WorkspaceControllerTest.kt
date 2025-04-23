package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.usecase.WorkspaceBuilder
import dev.sayaya.handbook.usecase.WorkspaceService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
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
import reactor.core.publisher.Mono

@WebFluxTest
@ContextConfiguration(classes = [ WorkspaceController::class, WorkspaceControllerTest.Companion.SecurityConfig::class ])
internal class WorkspaceControllerTest(
    private val webTestClient: WebTestClient,
    private val mockService: WorkspaceService
): ShouldSpec({
    val client = webTestClient.mutateWith(mockUser()).mutateWith(csrf())

    should("올바른 저장 요청 시 올바른 결과를 반환해야 한다") {
        // Given: Mock된 서비스 응답 정의
        val param = WorkspaceBuilder(name="test")
        val expected = param.build()
        every { mockService.save(any(), param) } returns Mono.just(expected)

        // When: API 호출
        client.post()
            .uri { builder -> builder.path("/workspace").build() }
            .bodyValue(param)
            .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(expected.id)
            .jsonPath("$.name").isEqualTo(expected.name)
    }
    should("잘못된 요청이 들어오면 400 BAD_REQUEST를 반환해야 한다") {
        val param = mapOf("invalid" to "value")
        client.post()
            .uri { builder -> builder.path("/workspace").build() }
            .bodyValue(param)
            .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json"))
            .exchange()
            .expectStatus().isBadRequest
    }
}) {
    companion object {
        @TestConfiguration
        @EnableWebFluxSecurity
        class SecurityConfig {
            @Bean fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
                anonymous { }
            }
            @Bean fun workspaceService(): WorkspaceService = mockk()
        }
    }
}
