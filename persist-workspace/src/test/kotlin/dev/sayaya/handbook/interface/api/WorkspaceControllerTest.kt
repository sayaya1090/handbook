package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.usecase.WorkspaceBuilder
import dev.sayaya.handbook.usecase.WorkspaceService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@ExtendWith(SpringExtension::class)
internal class WorkspaceControllerTest : ShouldSpec({
    val mockService = mockk<WorkspaceService>()
    val controller = WorkspaceController(mockService)
    val webTestClient = WebTestClient.bindToController(controller).build()
    should("올바른 저장 요청 시 올바른 결과를 반환해야 한다") {
        // Given: Mock된 서비스 응답 정의
        val param = WorkspaceBuilder(name="test")
        val expected = param.build()

        every { mockService.save(param) } returns Mono.just(expected)

        // When: API 호출
        webTestClient.post()
            .uri { builder -> builder.path("/workspace").build() }
            .bodyValue(param)
            .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(expected.id)
            .jsonPath("$.name").isEqualTo(expected.name)
    }
    should("잘못된 요청이 들어오면 400 BAD_REQUEST를 반환해야 한다") {
        val param = mapOf("invalid" to "value")
        webTestClient.post()
            .uri { builder -> builder.path("/workspace").build() }
            .bodyValue(param)
            .accept(MediaType.parseMediaType("application/vnd.sayaya.handbook.v1+json")).exchange()
            .expectStatus().isBadRequest
    }
})
