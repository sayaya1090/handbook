package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.domain.Menu
import dev.sayaya.handbook.usecase.MenuService
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux

class MenuControllerTest : DescribeSpec({
    val menuService = mockk<MenuService>()
    val controller = MenuController(menuService)
    val webTestClient = WebTestClient.bindToController(controller).build()

    val menu1 = Menu.builder().title("메뉴1").build()
    val menu2 = Menu.builder().title("메뉴2").build()

    describe("MenuController는") {
        it("메뉴 목록을 성공적으로 반환한다") {
            every { menuService.menus(any<Map<String, List<String>>>()) } returns Flux.just(menu1, menu2)

            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(2)
                .contains(menu1, menu2)
        }

        it("빈 메뉴 목록을 반환할 수 있다") {
            every { menuService.menus(any<Map<String, List<String>>>()) } returns Flux.empty()

            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(0)
        }

        it("서비스 에러 시 5xx를 반환한다") {
            every { menuService.menus(any<Map<String, List<String>>>()) } returns Flux.error(RuntimeException("실패"))

            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().is5xxServerError
        }
    }
})
