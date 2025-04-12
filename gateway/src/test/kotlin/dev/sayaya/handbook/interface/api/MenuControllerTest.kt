package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import dev.sayaya.handbook.usecase.MenuService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

internal class MenuControllerTest : ShouldSpec({
    // Mock objects
    val menuService = mockk<MenuService>()
    val controller = MenuController(menuService)

    // WebTestClient setup
    val webTestClient = WebTestClient
        .bindToController(controller)
        .build()

    // Test fixtures
    val menu1 = Menu.builder().title("메뉴1").build()
    val menu2 = Menu.builder().title("메뉴2").build()
    val mockRequest = mockk<ServerHttpRequest>()
    val mockHeaders = mockk<HttpHeaders>()

    beforeTest {
        every { mockRequest.headers } returns mockHeaders
    }

    context("메뉴 컨트롤러") {
        should("메뉴를 요청하면 목록을 성공적으로 반환한다") {
            // Given
            every { menuService.menus(any()) } returns Flux.just(menu1, menu2)

            // When & Then
            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(2)
                .contains(menu1, menu2)
        }

        should("메뉴 서비스에서 에러가 발생하면 적절한 에러를 반환한다") {
            // Given
            every { menuService.menus(any()) } returns Flux.error(RuntimeException("메뉴 조회 실패"))

            // When & Then
            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().is5xxServerError
        }

        should("빈 메뉴 목록을 반환할 수 있다") {
            // Given
            every { menuService.menus(any()) } returns Flux.empty()

            // When & Then
            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(0)
        }
    }
})