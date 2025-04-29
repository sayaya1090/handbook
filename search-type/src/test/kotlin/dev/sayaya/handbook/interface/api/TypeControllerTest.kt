package dev.sayaya.handbook.`interface`.api

import dev.sayaya.handbook.domain.Type
import dev.sayaya.handbook.domain.TypeWithLayout
import dev.sayaya.handbook.usecase.LayoutService
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.time.Instant
import java.util.*

@Suppress("ReactiveStreamsUnusedPublisher")
@ExtendWith(SpringExtension::class)
internal class TypeControllerTest : ShouldSpec({
    val mockService = mockk<LayoutService>()
    val controller = TypeController(mockService)
    val webTestClient = WebTestClient.bindToController(controller).build()
    val workspace = UUID.fromString("398f6038-2192-417b-914a-f74e4bf52451")
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
        every { mockService.search(workspace, any()) } returns Flux.fromIterable(expectedTypes)

        // When: API 호출
        webTestClient.get().uri { builder ->
            builder.path("/workspace/$workspace/types")
                .queryParam("basetime", baseTime.toEpochMilli())
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
})
