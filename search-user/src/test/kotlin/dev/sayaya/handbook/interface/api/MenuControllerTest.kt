package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@ContextConfiguration(classes = [ MenuController::class, MenuControllerTest.Companion.SecurityConfig::class, JsonConfig::class ])
class MenuControllerTest(private val client: WebTestClient) : ShouldSpec({

    // 목 객체 생성
    val mockRequest = mockk<ServerHttpRequest>()
    val mockHeaders = mockk<HttpHeaders>()

    // 테스트 준비
    beforeTest {
        every { mockRequest.headers } returns mockHeaders
    }

    context("미인증 사용자") {
        should("메뉴 요청시 빈 값을 반환한다") {
            client.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(0)
        }
    }
    context("인증된 사용자") {
        val authenticated = client.mutateWith(mockUser())
        should("메뉴 요청시 정의된 메뉴를 성공적으로 반환한다") {
            // Given
            val expectedMenu = MenuController.MENU

            // When & Then
            authenticated.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType("application/vnd.sayaya.handbook.v1+json")
                .expectBodyList(Menu::class.java)
                .hasSize(1)
                .contains(expectedMenu)
        }
    }
}) {
    companion object {
        @TestConfiguration
        @EnableWebFluxSecurity
        class SecurityConfig {
            @Bean
            fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
            }
        }
    }
}