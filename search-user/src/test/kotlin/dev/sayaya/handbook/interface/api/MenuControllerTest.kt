package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.client.domain.Menu
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.test.web.reactive.server.WebTestClient

class MenuControllerTest : ShouldSpec({
    // 컨트롤러 인스턴스 생성
    val controller = MenuController()

    // WebTestClient 설정
    val webTestClient = WebTestClient
        .bindToController(controller)
        .build()

    // 목 객체 생성
    val mockRequest = mockk<ServerHttpRequest>()
    val mockHeaders = mockk<HttpHeaders>()

    // 테스트 준비
    beforeTest {
        every { mockRequest.headers } returns mockHeaders
    }

    context("메뉴 컨트롤러") {
        should("메뉴 요청시 정의된 메뉴를 성공적으로 반환한다") {
            // Given
            val expectedMenu = MenuController.MENU

            // When & Then
            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .hasSize(1)
                .contains(expectedMenu)
        }

        should("Content-Type이 예상한 값과 일치하는지 확인한다") {
            // When & Then
            webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType("application/vnd.sayaya.handbook.v1+json")
        }

        should("메뉴의 tools 배열이 예상한 크기와 일치하는지 확인한다") {
            // When
            val result = webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .returnResult()
                .responseBody

            // Then
            result!!.size shouldBe 1
            result[0].tools().size shouldBe 1
        }

        should("메뉴에 정의된 각 툴의 타이틀이 예상과 일치하는지 확인한다") {
            // Expected tool titles
            val expectedToolTitles = listOf("user info")

            // When
            val result = webTestClient.get().uri("/menus")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Menu::class.java)
                .returnResult()
                .responseBody

            // Then
            val actualToolTitles = result!![0].tools().map { it.title() }
            actualToolTitles shouldContainExactly expectedToolTitles
        }
    }
})