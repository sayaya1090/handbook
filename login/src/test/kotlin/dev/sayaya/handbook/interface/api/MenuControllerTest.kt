package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.usecase.JsonConfig
import io.kotest.core.spec.style.BehaviorSpec
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@ContextConfiguration(classes = [ MenuController::class, MenuControllerTest.Companion.SecurityConfig::class, JsonConfig::class ])
internal class MenuControllerTest(private val client: WebTestClient): BehaviorSpec({
    Given("인증되지 않은 사용자가") {
        When("메뉴를 요청하면") {
            val request = client.get().uri("/menus")
            Then("로그인 메뉴를 출력한다") {
                request.exchange()
                    .expectStatus().isOk
                    .expectBody().jsonPath("$[0].title").isEqualTo("sign in")
            }
        }
    }
    Given("인증된 사용자가") {
        val authenticated = client.mutateWith(SecurityMockServerConfigurers.mockUser())
        When("메뉴를 요청하면") {
            val request = authenticated.get().uri("/menus")
            Then("로그아웃 메뉴를 출력한다") {
                request.exchange()
                    .expectStatus().isOk
                    .expectBody().jsonPath("$[0].title").isEqualTo("sign out")
            }
        }
    }
}){
    companion object {
        @TestConfiguration
        @EnableWebFluxSecurity
        class SecurityConfig {
            @Bean fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
                anonymous {  }
            }
        }
    }
}